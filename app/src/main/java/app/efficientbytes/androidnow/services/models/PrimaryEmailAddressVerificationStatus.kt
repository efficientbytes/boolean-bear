package app.efficientbytes.androidnow.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrimaryEmailAddressVerificationStatus(
    val message: String? = null,
    val emailAddress: String? = null
)