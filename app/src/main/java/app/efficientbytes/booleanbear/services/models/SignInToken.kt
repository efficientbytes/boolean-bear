package app.efficientbytes.booleanbear.services.models

import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignInToken(
    val userAccountId: String,
    val token: String,
    val basicProfileDetailsUpdated: Boolean,
    val passwordCreated: Boolean,
    val phoneNumberData: PhoneNumber,
    val singleDeviceLogin: SingleDeviceLogin
)

@JsonClass(generateAdapter = true)
data class SignInTokenResponse(
    val data: SignInToken? = null,
    val message: String? = null
)