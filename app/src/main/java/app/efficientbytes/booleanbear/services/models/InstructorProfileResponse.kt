package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteInstructorProfile(
    val instructorId: String,
    val firstName: String,
    val lastName: String? = null,
    val bio: String? = null,
    val oneLineDescription: String? = null,
    val profession: String? = null,
    val workingAt: String? = null,
    val profileImage: String? = null,
    val coverImage: String? = null,
    val gitHubUsername: String? = null,
    val linkedInUsername: String? = null,
    val skills: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class InstructorProfileResponse(
    val data: RemoteInstructorProfile? = null,
    val message: String? = null
)

