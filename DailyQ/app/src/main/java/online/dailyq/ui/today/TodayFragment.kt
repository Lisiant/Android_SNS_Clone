package online.dailyq.ui.today

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import online.dailyq.R
import online.dailyq.api.response.Question
import online.dailyq.databinding.FragmentTodayBinding
import online.dailyq.ui.base.BaseFragment
import online.dailyq.ui.write.WriteActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayFragment : BaseFragment() {
    var _binding: FragmentTodayBinding? = null
    val binding get() = _binding!!
    var question: Question? = null

    // registerForActivityResult으로 콜백을 동록
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    setupAnswer()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // writeButton, editButton 클릭 시 WriteActivity로 전환
        // intent로 정보 전달(qid, mode)
        binding.writeButton.setOnClickListener {
            startForResult.launch(Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra(WriteActivity.EXTRA_QID, question!!.id)
                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.WRITE)
            })
        }

        binding.editButton.setOnClickListener {
            startForResult.launch(Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra(WriteActivity.EXTRA_QID, question!!.id)
                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.EDIT)
            })
        }

        // 삭제 시 dialog 표시하여 확인 절차 거침
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {

            val questionResponse = api.getQuestion(LocalDate.now())
            if (questionResponse.isSuccessful) {
                question = questionResponse.body()!!
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy. M. d.")
                binding.date.text = dateFormatter.format(question!!.id)
                binding.question.text = question!!.text

                setupAnswer()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // 데이터를 가져와 UI 구성하는 코드 중복 제거
    suspend fun setupAnswer() {
        val question = question ?: return

        val answer = api.getAnswer(question.id).body()
        binding.answerArea.isVisible = (answer != null)
        binding.textAnswer.text = answer?.text

        binding.writeButton.isVisible = (answer == null)

    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.dialog_msg_are_you_sure_to_delete)
            .setPositiveButton(R.string.ok) { dialog, which ->
                // 확인 버튼 클릭 시 deleteResponse 호출
                lifecycleScope.launch {
                    val deleteResponse = api.deleteAnswer(question!!.id)
                    if (deleteResponse.isSuccessful) {
                        binding.answerArea.isVisible = false
                        binding.writeButton.isVisible = true
                    }
                }
            }.setNegativeButton(R.string.cancel) { dialog, which ->
                // 취소 버튼 클릭 시 아무 동작 X
            }.show()
    }


}