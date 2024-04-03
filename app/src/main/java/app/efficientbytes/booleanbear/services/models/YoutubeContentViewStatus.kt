package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeContentViewStatus(
    val youtubeContentView: YoutubeContentView,
    val message: String
)
