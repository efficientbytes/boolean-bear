package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.SHUFFLED_CONTENT_TABLE
import app.efficientbytes.booleanbear.utils.SHUFFLED_CONTENT_TABLE_FTS

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
    val hashTags: List<String>? = null
)

@Entity(tableName = SHUFFLED_CONTENT_TABLE_FTS)
@Fts4(contentEntity = LocalShuffledContent::class)
data class LocalShuffledContentFTS(
    val categoryId: String,
    val contentId: String,
    val title: String,
    val hashTags: List<String>? = null
)