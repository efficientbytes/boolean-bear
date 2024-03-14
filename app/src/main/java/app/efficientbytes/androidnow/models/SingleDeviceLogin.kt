package app.efficientbytes.androidnow.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.androidnow.utils.SINGLE_DEVICE_LOGIN_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = SINGLE_DEVICE_LOGIN_TABLE)
data class SingleDeviceLogin(
    @PrimaryKey(autoGenerate = false)
    val deviceId: String,
    val createdOn: Long,
    val message: String? = null,
)