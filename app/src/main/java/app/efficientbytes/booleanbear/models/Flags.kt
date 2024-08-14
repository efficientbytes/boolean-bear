package app.efficientbytes.booleanbear.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.Pi.BOOLEAN_FLAG_TABLE

@Entity(tableName = BOOLEAN_FLAG_TABLE)
data class LocalBooleanFlag(
    @PrimaryKey(autoGenerate = false)
    val flagKey: String,
    val value: Boolean
)