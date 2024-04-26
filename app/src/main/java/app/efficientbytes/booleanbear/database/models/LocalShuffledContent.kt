package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.SHUFFLED_CONTENT_TABLE

@Entity(tableName = SHUFFLED_CONTENT_TABLE)
data class LocalShuffledContent(
    val categoryId: String,
    @PrimaryKey(autoGenerate = false)
    val contentId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String,
    val hashTags: List<String>?=null
)