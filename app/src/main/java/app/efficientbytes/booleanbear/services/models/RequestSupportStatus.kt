package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestSupportStatus(
    val ticketId: String,
    val message: String
)
