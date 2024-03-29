package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceContentCategory(
    val id: String,
    val index : Int,
    val title: String,
    val caption: String,
    val contentCount: Int,
    val deepLink: String,
    val type1Thumbnail: String,
    val contentIds: List<String>,
    val searchTags: List<String>,
    val dateCreated: Long,
    val dateModified: Long
)
