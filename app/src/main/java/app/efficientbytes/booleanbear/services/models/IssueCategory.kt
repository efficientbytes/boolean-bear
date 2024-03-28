package app.efficientbytes.booleanbear.services.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.ISSUE_CATEGORY_ADAPTER_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = ISSUE_CATEGORY_ADAPTER_TABLE)
data class IssueCategory(
    @PrimaryKey(autoGenerate = false)
    val index: Int,
    val name: String
)
