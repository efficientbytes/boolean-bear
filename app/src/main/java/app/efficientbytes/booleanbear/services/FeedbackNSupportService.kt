package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.Feedback
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.RequestSupportStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackNSupportService {

    @POST("app/feedback/")
    suspend fun uploadFeedback(@Body feedback: Feedback): Response<Feedback>

    @POST("app/contact-support/")
    suspend fun requestSupport(@Body requestSupport: RequestSupport): Response<RequestSupportStatus>

}