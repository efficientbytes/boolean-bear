package app.efficientbytes.androidnow.services.models

import app.efficientbytes.androidnow.models.UserProfile
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfilePayload(
    val signOut:Boolean=true,
    val userProfile: UserProfile? = null,
    val message: String? = null
)