package app.efficientbytes.booleanbear.services.models

import app.efficientbytes.booleanbear.models.UserProfile
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfilePayload(
    val signOut:Boolean=true,
    val userProfile: UserProfile? = null,
    val message: String? = null
)