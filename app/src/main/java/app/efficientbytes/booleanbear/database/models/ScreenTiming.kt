package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.USER_SCREEN_TIMING_TABLE

@Entity(tableName = USER_SCREEN_TIMING_TABLE)
data class ScreenTiming(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val date: Long,
    val opened: Long,
    var closed: Long = -1,
)
