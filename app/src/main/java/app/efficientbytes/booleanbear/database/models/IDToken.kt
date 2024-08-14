package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.Pi.ID_TOKEN_TABLE

@Entity(tableName = ID_TOKEN_TABLE)
data class IDToken(
    @PrimaryKey(autoGenerate = false)
    val rowId: Int? = 1,
    val token: String
)
