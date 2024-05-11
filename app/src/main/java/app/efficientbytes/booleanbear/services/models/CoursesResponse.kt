package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteCourseTopic(
    val topicId: String,
    val topic: String,
    val displayIndex: Int,
    val type1Thumbnail: String,
)

@JsonClass(generateAdapter = true)
data class RemoteCourse(
    val courseId: String,
    val title: String,
    val type1Thumbnail: String,
    val isAvailable: Boolean,
    val nonAvailabilityReason: String? = null,
    val hashTags: List<String>,
    val createdOn: Long,
    val topicId: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteCourseBundles(
    val topicDetails: RemoteCourseTopic,
    val courseList: List<RemoteCourse>
)

@JsonClass(generateAdapter = true)
data class RemoteCourseBundleResponse(
    val data: List<RemoteCourseBundles>? = null,
    val message: String? = null
)