package app.efficientbytes.androidnow.services.models

import app.efficientbytes.androidnow.models.UserProfile

data class UserProfilePayload(
    val userProfile: UserProfile? = null,
    val message: String? = null
)