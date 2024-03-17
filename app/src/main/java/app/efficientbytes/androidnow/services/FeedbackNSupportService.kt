package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.services.models.Feedback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackNSupportService {

    @POST("app/feedback/")
    suspend fun uploadFeedback(@Body feedback: Feedback): Response<Feedback>

    @POST("contact-support/")
    suspend fun requestSupport()

}