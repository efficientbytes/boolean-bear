package app.efficientbytes.androidnow.services.models

import com.google.gson.annotations.Expose
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhoneNumberVerificationStatus(
    @Json(name = "message")
    @Expose
    val message: String?=null,
    @Json(name = "phoneNumber")
    @Expose
    val phoneNumber: String?=null
)