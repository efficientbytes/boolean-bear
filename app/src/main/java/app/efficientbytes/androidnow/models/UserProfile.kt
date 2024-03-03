package app.efficientbytes.androidnow.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.androidnow.utils.USER_PROFILE_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = USER_PROFILE_TABLE)
data class UserProfile(
    val firstName: String,
    val phoneNumber: String,
    val phoneNumberPrefix: String,
    val completePhoneNumber: String,
    val userAccountId: String,
    val activityId: String? = null,
    val profession: String? = null,
    val fcmToken: String? = null,
    val lastName: String? = null,
    val emailAddress: String? = null,
    val linkedInAddress: String? = null,
    val gitHubUsername: String? = null,
    val universityName: String? = null,
    val createdOn: Long? = null,
    val lastUpdatedOn: Long? = null,
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "rowId")
    var rowId: Int? = 1
)
