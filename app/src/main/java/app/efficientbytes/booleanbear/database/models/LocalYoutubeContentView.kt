package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.LOCAL_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE

@Entity(tableName = LOCAL_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE)
data class LocalYoutubeContentView(
    val categoryId: String,
    @PrimaryKey(autoGenerate = false)
    val contentId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String
)