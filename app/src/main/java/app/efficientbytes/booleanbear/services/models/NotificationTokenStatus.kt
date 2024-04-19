package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationTokenStatus(
    val notificationToken: NotificationToken? = null,
    val message: String? = null
)
