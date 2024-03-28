package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.FeedbackNSupportService
import app.efficientbytes.booleanbear.services.models.Feedback
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.RequestSupportStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FeedbackNSupportRepository(private val feedbackNSupportService: FeedbackNSupportService) {

    private val gson = Gson()
    suspend fun postFeedback(feedback: Feedback) = flow {
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

    suspend fun requestSupport(requestSupport: RequestSupport) = flow {
        emit(DataStatus.loading())
        val response = feedbackNSupportService.requestSupport(requestSupport)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                emit(DataStatus.success(response.body()))
            }

            responseCode >= 400 -> {
                val errorResponse: RequestSupportStatus = gson.fromJson(
                    response.errorBody()!!.string(),
                    RequestSupportStatus::class.java
                )
                emit(DataStatus.failed(errorResponse.message))
            }
        }
    }

}