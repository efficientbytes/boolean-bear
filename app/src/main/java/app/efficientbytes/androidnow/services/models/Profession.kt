package app.efficientbytes.androidnow.services.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.androidnow.utils.PROFESSION_ADAPTER_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = PROFESSION_ADAPTER_TABLE)
data class Profession(
    @PrimaryKey(autoGenerate = false)
    val index: Int,
    val name: String
)
