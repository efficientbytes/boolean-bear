package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InstructorProfileStatus(
    val instructorProfile: RemoteInstructorProfile? = null,
    val message: String? = null
)
