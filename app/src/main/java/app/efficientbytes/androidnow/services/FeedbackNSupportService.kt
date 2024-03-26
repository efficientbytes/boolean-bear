package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.services.models.Feedback
import app.efficientbytes.androidnow.services.models.RequestSupport
import app.efficientbytes.androidnow.services.models.RequestSupportStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackNSupportService {

    @POST("app/feedback/")
    suspend fun uploadFeedback(@Body feedback: Feedback): Response<Feedback>

    @POST("app/contact-support/")
    suspend fun requestSupport(@Body requestSupport: RequestSupport): Response<RequestSupportStatus>

}