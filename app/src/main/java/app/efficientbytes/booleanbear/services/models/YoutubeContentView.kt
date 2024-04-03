package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeContentView(
    val contentId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String
)