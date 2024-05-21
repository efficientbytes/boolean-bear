package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.RequestSupportResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface FeedbackNSupportService {

    @FormUrlEncoded
    @POST("app/feedback/")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun uploadFeedback(
        @Field("feedback") feedback: String,
    ): Response<ResponseMessage>

    @FormUrlEncoded
    @POST("app/contact-support/")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun requestSupport(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("category") category: Int,
        @Field("prefix") prefix: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("userAccountId") userAccountId: String? = null,
    ): Response<RequestSupportResponse>

}