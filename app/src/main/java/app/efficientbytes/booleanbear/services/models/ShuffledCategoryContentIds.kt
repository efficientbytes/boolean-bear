package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShuffledCategoryContentIds(
    val contentIds: List<String>,
    val message: String
)
