package online.dailyq.ui.profile

import androidx.paging.PagingSource
import androidx.paging.PagingState
import online.dailyq.api.ApiService
import online.dailyq.api.response.QuestionAndAnswer
import java.time.LocalDate


// 사용자의 답도 타임라인처럼 나눠서 가져올 수 있도록 PagingSource를 확장해 UserAnswerPagingSource를 만든다.
class UserAnswerPagingSource(val api: ApiService, val uid: String) :
    PagingSource<LocalDate, QuestionAndAnswer>() {

    override fun getRefreshKey(state: PagingState<LocalDate, QuestionAndAnswer>): LocalDate? = null

    //
    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, QuestionAndAnswer> {
        val userAnswersResponse = api.getUserAnswers(uid, params.key)
        return if (userAnswersResponse.isSuccessful) {
            val userAnswers = userAnswersResponse.body()!!

            val nextKey = if (userAnswers.isNotEmpty()) {
                userAnswers.minOf { it.question.id }
            } else {
                null
            }
            LoadResult.Page(
                data = userAnswers,
                prevKey = null,
                nextKey = nextKey
            )
        } else {
            LoadResult.Error(Throwable("Paging Error"))
        }
    }
}