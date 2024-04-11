package app.efficientbytes.booleanbear.services.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.REMOTE_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = REMOTE_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE)
data class YoutubeContentView(
    @PrimaryKey(autoGenerate = false)
    val contentId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String
)