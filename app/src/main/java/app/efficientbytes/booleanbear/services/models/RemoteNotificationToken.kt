package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteNotificationToken(
    val token: String,
    val userAccountId: String? = null
)
