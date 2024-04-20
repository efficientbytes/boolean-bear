package app.efficientbytes.booleanbear.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.SINGLE_DEVICE_LOGIN_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SingleDeviceLoginResponse(
    val data: SingleDeviceLogin? = null,
    val message: String? = null,
)

@JsonClass(generateAdapter = true)
@Entity(tableName = SINGLE_DEVICE_LOGIN_TABLE)
data class SingleDeviceLogin(
    @PrimaryKey(autoGenerate = false)
    val deviceId: String,
    val createdOn: Long? = null,
)
