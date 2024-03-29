package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.CATEGORY_CONTENT_ID_TABLE

@Entity(tableName = CATEGORY_CONTENT_ID_TABLE)
data class CategoryContentId(
    @PrimaryKey(autoGenerate = false)
    val categoryId: String,
    val contentIds: String,
)
