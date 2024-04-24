package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestContentCount(
    val contentId: String? = null,
    val message: String? = null
)
