package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteShuffledContent(
    val contentId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String,
    val hashTags:List<String>?=null
)

@JsonClass(generateAdapter = true)
data class ShuffledContentResponse(
    val data: RemoteShuffledContent? = null,
    val message: String? = null
)
