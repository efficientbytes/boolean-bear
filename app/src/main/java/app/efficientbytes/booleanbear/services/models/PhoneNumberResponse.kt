package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhoneNumber(
    val prefix: String,
    val phoneNumber: String
)

@JsonClass(generateAdapter = true)
data class VerifyPhoneResponse(
    val data: PhoneNumber? = null,
    val message: String? = null
)