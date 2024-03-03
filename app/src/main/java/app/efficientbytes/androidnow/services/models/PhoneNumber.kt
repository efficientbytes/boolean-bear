package app.efficientbytes.androidnow.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhoneNumber(
    val phoneNumber: String,
    val prefix: String? = null
)
