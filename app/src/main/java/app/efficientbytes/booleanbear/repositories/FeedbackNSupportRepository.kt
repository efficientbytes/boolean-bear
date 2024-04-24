package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.FeedbackNSupportService
import app.efficientbytes.booleanbear.services.models.Feedback
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.RequestSupportStatus
import app.efficientbytes.booleanbear.utils.NoInternetException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.SocketTimeoutException

class FeedbackNSupportRepository(private val feedbackNSupportService: FeedbackNSupportService) {

    private val gson = Gson()
    suspend fun postFeedback(feedback: Feedback) = flow {
        try {
            emit(DataStatus.loading())
            val response = feedbackNSupportService.uploadFeedback(
                feedback.feedback,
                feedback.userAccountId,
                feedback.message
            )
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun requestSupport(requestSupport: RequestSupport) = flow {
        try {
            emit(DataStatus.loading())
            val response = feedbackNSupportService.requestSupport(
                requestSupport.title,
                requestSupport.description,
                requestSupport.category,
                requestSupport.completePhoneNumber,
                requestSupport.userAccountId
            )
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

}