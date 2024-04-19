package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationTokenStatus(
    val remoteNotificationToken: RemoteNotificationToken? = null,
    val message: String? = null
)
