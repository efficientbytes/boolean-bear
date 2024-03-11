package app.efficientbytes.androidnow.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyPrimaryEmailAddress(
    val emailAddress: String? = null,
    val userAccountId: String? = null,
    val firstName: String? = null
)
