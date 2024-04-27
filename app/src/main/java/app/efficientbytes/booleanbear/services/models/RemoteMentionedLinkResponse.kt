package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteMentionedLink(
    val createdOn: Long? = -1L,
    val link: String? = null,
    val linkId: String,
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteMentionedLinkResponse(
    val data: RemoteMentionedLink? = null,
    val message: String? = null
)
