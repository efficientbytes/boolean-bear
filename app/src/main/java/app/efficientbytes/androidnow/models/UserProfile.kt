package app.efficientbytes.androidnow.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.androidnow.utils.USER_PROFILE_TABLE
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = USER_PROFILE_TABLE)
data class UserProfile(
    var firstName: String,
    var phoneNumber: String,
    var phoneNumberPrefix: String,
    var completePhoneNumber: String,
    var userAccountId: String,
    var activityId: String? = null,
    var profession: String? = null,
    var fcmToken: String? = null,
    var lastName: String? = null,
    var emailAddress: String? = null,
    var linkedInUsername: String? = null,
    var gitHubUsername: String? = null,
    var universityName: String? = null,
    var createdOn: Long? = null,
    var lastUpdatedOn: Long? = null,
    @Json(ignore = true)
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "rowId")
    var rowId: Int? = 1
)

object SingletonUserData{
    private var userProfile : UserProfile?=null
    fun getInstance() = userProfile

    fun setInstance(userProfile: UserProfile) {
        this.userProfile = userProfile
    }
}