package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShuffledCategoryContentIdListResponse(
    val data: List<String>,
    val message: String
)
