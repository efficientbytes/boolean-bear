package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.Pi.FCM_TOKEN_TABLE

@Entity(tableName = FCM_TOKEN_TABLE)
data class LocalNotificationToken(
    @PrimaryKey(autoGenerate = false)
    val userAccountId: String,
    val token: String
)
