package app.efficientbytes.androidnow.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyPhoneNumber(
    val phoneNumber: String,
    val otp: String? = null
)
