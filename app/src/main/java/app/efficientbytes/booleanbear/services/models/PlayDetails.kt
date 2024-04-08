package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayDetails(
    val contentId: String,
    val title: String,
    val description: String,
    val createdOn: Long,
    val updatedOn: Long,
    val instructorId: String,
    val showAds: Boolean = true,
    val language: String? = null,
    val runTime: Long? = 0L,
    val type1Thumbnail: String? = null,
    val instructorFirstName: String,
    val instructorLastName: String? = null,
    val instructorProfilePic: String? = null,
    val nextSuggestion: String? = null,
    val message: String? = null
)
