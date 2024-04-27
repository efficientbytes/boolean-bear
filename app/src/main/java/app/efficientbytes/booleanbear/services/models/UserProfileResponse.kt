package app.efficientbytes.booleanbear.services.models

import app.efficientbytes.booleanbear.models.UserProfile
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class UserProfileResponse(
    val data: UserProfile? = null,
    val message: String? = null
)