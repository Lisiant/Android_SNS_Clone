package online.dailyq.ui.write

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
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
 * 질문: WriteActvity를 시작하는 곳에서 intent를 통해서 전달받는다.
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
    var imageUrl: String? = null

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK){
                lifecycleScope.launch{
                    val imageUrl = result.data?.data ?: return@launch
                    val requestBody = imageUrl.asRequestBody(contentResolver)

                    // createFormdata
                    val part = MultipartBody.Part.createFormData("image", )
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
        }
    }

    // 앱바에 메뉴 설정하기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 완료 버튼을 눌렀을 때 write() 실행
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.done -> {
                write()
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
                api.writeAnswer(question.id, text)
            } else {
                api.editAnswer(question.id, text)
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