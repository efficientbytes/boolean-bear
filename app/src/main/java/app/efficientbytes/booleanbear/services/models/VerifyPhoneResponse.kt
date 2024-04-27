package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhoneNumber(
    val phoneNumber: String,
    val prefix: String? = null
)

@JsonClass(generateAdapter = true)
data class PhoneOTP(
    val phoneNumber: String,
    val otp: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyPhoneResponse(
    val message: String? = null,
    val phoneNumber: String? = null
)