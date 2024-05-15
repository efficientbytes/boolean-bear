package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.COURSE_TABLE
import app.efficientbytes.booleanbear.utils.COURSE_TABLE_FTS
import app.efficientbytes.booleanbear.utils.COURSE_TOPIC_TABLE
import app.efficientbytes.booleanbear.utils.WAITING_LIST_COURSE_TABLE

@Entity(tableName = COURSE_TOPIC_TABLE)
data class LocalCourseTopic(
    @PrimaryKey(autoGenerate = false)
    val topicId: String,
    val topic: String,
    val displayIndex: Int,
    val type1Thumbnail: String,
)

@Entity(tableName = COURSE_TABLE)
data class LocalCourse(
    @PrimaryKey(autoGenerate = false)
    val courseId: String,
    val title: String,
    val type1Thumbnail: String,
    val isAvailable: Boolean,
    val nonAvailabilityReason: String? = null,
    val hashTags: List<String>,
    val createdOn: Long,
    val topicId: String? = null
)

@Entity(tableName = COURSE_TABLE_FTS)
@Fts4(contentEntity = LocalCourse::class)
data class LocalCourseFTS(
    val courseId: String,
    val topicId: String,
    val title: String,
    val hashTags: List<String>
)

@Entity(tableName = WAITING_LIST_COURSE_TABLE)
data class LocalWaitingListCourse(
    @PrimaryKey(autoGenerate = false)
    val courseId: String
)