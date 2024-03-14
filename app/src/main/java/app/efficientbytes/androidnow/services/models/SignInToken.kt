package app.efficientbytes.androidnow.services.models

import app.efficientbytes.androidnow.models.SingleDeviceLogin
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignInToken(
    val token: String? = null,
    val basicProfileDetailsUpdated: Boolean? = null,
    val message: String? = null,
    val userAccountId: String? = null,
    val singleDeviceLogin: SingleDeviceLogin? = null
)
