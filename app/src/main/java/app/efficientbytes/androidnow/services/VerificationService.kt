package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.androidnow.services.models.PrimaryEmailAddressVerificationStatus
import app.efficientbytes.androidnow.services.models.VerifyPhoneNumber
import app.efficientbytes.androidnow.services.models.VerifyPrimaryEmailAddress
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationService {

    @POST("verification/phone-number/send-otp")
    suspend fun sendOtpToPhoneNumber(
        @Body sendPhoneNumberOTP: VerifyPhoneNumber
    ): Response<PhoneNumberVerificationStatus>

    @POST("verification/phone-number/verify-otp")
    suspend fun verifyPhoneNumberOTP(
        @Body verifyOTP: VerifyPhoneNumber
    ): Response<PhoneNumberVerificationStatus>

    @POST("verification/primary-mail/send-verification-link")
    suspend fun verifyPrimaryEmailAddress(
        @Body verifyPrimaryEmailAddress: VerifyPrimaryEmailAddress
    ): Response<PrimaryEmailAddressVerificationStatus>

}