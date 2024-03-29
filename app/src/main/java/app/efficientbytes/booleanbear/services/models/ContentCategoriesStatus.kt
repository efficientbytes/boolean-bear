package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContentCategoriesStatus(
    val categoryList: List<ServiceContentCategory>,
    val message: String
)
