package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginMode(
    val phoneNumber: String,
    val prefix: String? = null,
    val userAccountId: String? = null,
    val mode: Int,
)

@JsonClass(generateAdapter = true)
data class LoginModeResponse(
    val data: LoginMode? = null,
    val message: String? = null
)