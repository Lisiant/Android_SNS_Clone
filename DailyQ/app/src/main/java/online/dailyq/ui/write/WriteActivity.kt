package online.dailyq.ui.write

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import online.dailyq.R
import online.dailyq.api.asRequestBody
import online.dailyq.api.response.Answer
import online.dailyq.api.response.Question
import online.dailyq.databinding.ActivityWriteBinding
import online.dailyq.ui.base.BaseActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 답을 작성하거나 수정하는 액티비티
 *
 * Question: WriteActvity를 시작하는 곳에서 intent를 통해서 전달받는다.
 */
class WriteActivity : BaseActivity() {
    companion object {
        const val EXTRA_QID = "qid"
        const val EXTRA_MODE = "mode"
    }

    enum class Mode {
        WRITE, EDIT
    }

    lateinit var binding: ActivityWriteBinding
    lateinit var mode: Mode
    lateinit var question: Question
    var answer: Answer? = null

    // 이미지 API에서 받아온 Url을 담는 변수
    var imageUrl: String? = null

    // 이후 onOptionsItemSelected에서 이미지를 받아오는 액션을 수행할 때 사용
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    // Url이 아니라 Uri임에 주의
                    val imageUri = result.data?.data ?: return@launch

                    // Extensions.kt 에서 작성한 확장 함수 사용
                    val requestBody = imageUri.asRequestBody(contentResolver)

                    // MultipartBody.Part 생성 후 이미지 업로드 수행하는 API 호출
                    val part = MultipartBody.Part.createFormData("image", "filename", requestBody)
                    val imageResponse = api.uploadImage(part)

                    // API 호출 성공적인 경우 (이미지 업로드에 성공한 경우) 하단 썸네일에 표시
                    if (imageResponse.isSuccessful) {
                        imageUrl = imageResponse.body()!!.url

                        // Coil 사용해서 imageView 에서 바로 확장함수 load 사용 가능
                        binding.photo.load(imageUrl) {
                            transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.thumbnail_rounded_corner)))
                        }
                        binding.photoArea.isVisible = true
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 전달받은 qid를 파싱해 타이틀에 날짜를 표시
        val qid = intent.getSerializableExtra(EXTRA_QID) as LocalDate
        mode = intent?.getSerializableExtra(EXTRA_MODE)!! as Mode

        supportActionBar?.title =
            DateTimeFormatter.ofPattern(getString(R.string.date_format)).format(qid)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // API로 질문과 내가 쓴 답을 가져와 UI에 표시
        lifecycleScope.launch {
            question = api.getQuestion(qid).body()!!
            answer = api.getAnswer(qid).body()

            binding.question.text = question.text
            binding.answer.setText(answer?.text)

            imageUrl = answer?.photo
            binding.photoArea.isVisible = !imageUrl.isNullOrEmpty()

            // let scope function 으로 load 확장함수의 매개변수로 imageUrl 전달.
            imageUrl?.let {
                binding.photo.load(it) {
                    transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.thumbnail_rounded_corner)))
                }
            }

        }

        binding.photoArea.setOnClickListener {
            showDeleteConfirmDialog()
        }

    }

    fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.dialog_msg_are_you_sure_to_delete)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                binding.photo.setImageResource(0)
                binding.photoArea.isVisible = false
                imageUrl = null
            }
            .setNegativeButton(android.R.string.cancel) { dialog, which ->

            }.show()

    }

    // 앱바에 메뉴 설정하기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 완료 버튼을 눌렀을 때 write() 실행
            R.id.done -> {
                write()
                return true
            }
            // 사진 추가 버튼을 눌렀을 때 사진 선택하기 화면으로 전환
            R.id.add_photo -> {

                startForResult.launch(
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                    })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 답을 수정하거나 작성하는 메서드, 코루틴으로 api 실행
    fun write() {
        val text = binding.answer.text.toString().trimEnd()
        lifecycleScope.launch {
            val answerResponse = if (answer == null) {
                api.writeAnswer(question.id, text, imageUrl)
            } else {
                api.editAnswer(question.id, text, imageUrl)
            }

            if (answerResponse.isSuccessful) {
                // Activity의 setResult으로 MainActivity에 값 전달
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this@WriteActivity, answerResponse.message(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}