package app.efficientbytes.androidnow.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteUserAccountStatus(
    val message: String,
)
