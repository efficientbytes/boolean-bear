package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.VerifyPhoneResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface VerificationService {

    @FormUrlEncoded
    @POST("verification/phone-number/send-otp")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun sendOtpToPhoneNumber(
        @Field("phoneNumber") phoneNumber: String,
        @Field("otp") otp: String? = null
    ): Response<VerifyPhoneResponse>

    @FormUrlEncoded
    @POST("verification/phone-number/verify-otp")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun verifyPhoneNumberOTP(
        @Field("phoneNumber") phoneNumber: String,
        @Field("otp") otp: String? = null
    ): Response<VerifyPhoneResponse>

    @FormUrlEncoded
    @POST("verification/primary-mail/send-verification-link")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun verifyPrimaryEmailAddress(
        @Field("emailAddress") emailAddress: String? = null,
        @Field("firstName") firstName: String? = null
    ): Response<ResponseMessage>

}