package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.LoginModeResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.VerifyPhoneResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface VerificationService {

    @FormUrlEncoded
    @POST("verification/login-mode")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun getLoginMode(
        @Field("prefix") prefix: String,
        @Field("phoneNumber") phoneNumber: String
    ): Response<LoginModeResponse>

    @FormUrlEncoded
    @POST("verification/phone-number/send-otp")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun sendOtpToPhoneNumber(
        @Field("prefix") prefix: String,
        @Field("phoneNumber") phoneNumber: String,
    ): Response<VerifyPhoneResponse>

    @FormUrlEncoded
    @POST("verification/phone-number/verify-otp")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun verifyPhoneNumberOTP(
        @Field("prefix") prefix: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("otp") otp: String
    ): Response<VerifyPhoneResponse>

    @FormUrlEncoded
    @POST("verification/primary-mail/send-verification-link")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun verifyPrimaryEmailAddress(
        @Field("emailAddress") emailAddress: String? = null,
        @Field("firstName") firstName: String? = null
    ): Response<ResponseMessage>

}