package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.FeedbackNSupportService
import app.efficientbytes.androidnow.services.models.Feedback
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FeedbackNSupportRepository(private val feedbackNSupportService: FeedbackNSupportService) {

    private val gson = Gson()
    suspend fun uploadFeedback(feedback: Feedback) = flow {
        emit(DataStatus.loading())
        val response = feedbackNSupportService.uploadFeedback(feedback)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                emit(DataStatus.success(response.body()))
            }

            responseCode >= 400 -> {
                val errorResponse: Feedback = gson.fromJson(
                    response.errorBody()!!.string(),
                    Feedback::class.java
                )
                emit(DataStatus.failed(errorResponse.message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

}