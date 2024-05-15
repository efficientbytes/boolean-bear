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
data class RemoteCourseBundle(
    val topicDetails: RemoteCourseTopic,
    val courseList: List<RemoteCourse>
)

@JsonClass(generateAdapter = true)
data class RemoteCourseBundleResponse(
    val data: List<RemoteCourseBundle>? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteWaitingListCourse(
    val courseId: String
)

@JsonClass(generateAdapter = true)
data class WaitingListCourseResponse(
    val data: RemoteWaitingListCourse? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class WaitingListCoursesResponse(
    val data: List<String>? = null,
    val message: String? = null
)