package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteReel(
    val topicId: String?=null,
    val reelId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String,
    val hashTags: List<String>
)

@JsonClass(generateAdapter = true)
data class ReelResponse(
    val message: String? = null,
    val data: RemoteReel? = null
)

@JsonClass(generateAdapter = true)
data class ReelsResponse(
    val message: String? = null,
    val data: List<RemoteReel>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteReelTopic(
    val topicId: String,
    val topic: String,
    val displayIndex: Int,
    val type1Thumbnail: String,
)

@JsonClass(generateAdapter = true)
data class ReelTopicsResponse(
    val message: String? = null,
    val data: List<RemoteReelTopic>? = null
)

@JsonClass(generateAdapter = true)
data class ReelDetails(
    val reelId: String,
    val title: String,
    val description: String,
    val createdOn: Long,
    val updatedOn: Long,
    val instructorId: String,
    val showAds: Boolean,
    val language: String,
    val runTime: Long,
    val type1Thumbnail: String,
    val instructorFirstName: String,
    val instructorLastName: String? = null,
    val instructorProfilePic: String,
    val nextReelId: String?=null,
    val hashTags: List<String>?=null,
    val mentionedLinkIds: List<String>?=null,
    val topicId: String?=null
)

@JsonClass(generateAdapter = true)
data class ReelDetailsResponse(
    val message: String? = null,
    val data: ReelDetails? = null
)

@JsonClass(generateAdapter = true)
data class ReelPlayLink(
    val data: String? = null,
    val message: String? = null
)
