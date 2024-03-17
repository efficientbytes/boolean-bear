package app.efficientbytes.androidnow.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Feedback(
    val feedback: String,
    val userAccountId: String,
    val message: String
)
