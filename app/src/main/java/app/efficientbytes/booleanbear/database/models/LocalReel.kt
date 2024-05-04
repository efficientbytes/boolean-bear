package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.REELS_TABLE
import app.efficientbytes.booleanbear.utils.REELS_TABLE_FTS

@Entity(tableName = REELS_TABLE)
data class LocalReel(
    val topicId: String,
    @PrimaryKey(autoGenerate = false)
    val reelId: String,
    val title: String,
    val instructorName: String,
    val createdOn: Long,
    val runTime: Long,
    val thumbnail: String,
    val hashTags: List<String>? = null
)

@Entity(tableName = REELS_TABLE_FTS)
@Fts4(contentEntity = LocalReel::class)
data class LocalReelFTS(
    val topicId: String,
    val reelId: String,
    val title: String,
    val hashTags: List<String>? = null
)