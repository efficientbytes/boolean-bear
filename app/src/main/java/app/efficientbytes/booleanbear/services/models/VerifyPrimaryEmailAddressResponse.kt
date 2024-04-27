package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyPrimaryEmailAddress(
    val emailAddress: String? = null,
    val firstName: String? = null
)
