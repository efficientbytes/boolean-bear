package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.SHUFFLED_CATEGORY_TABLE

@Entity(tableName = SHUFFLED_CATEGORY_TABLE)
data class ShuffledCategory(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val index : Int,
    val title: String,
    val caption: String,
    val contentCount: Int,
    val deepLink: String,
    val type1Thumbnail: String,
    val dateCreated: Long,
    val dateModified: Long
)
