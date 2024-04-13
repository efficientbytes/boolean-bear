package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteMentionedLinkStatus(
    val mentionedLink: RemoteMentionedLink? = null,
    val message: String? = null
)
