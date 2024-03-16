package app.efficientbytes.androidnow.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.androidnow.utils.USER_PROFILE_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = USER_PROFILE_TABLE)
data class UserProfile(
    var firstName: String,
    var phoneNumber: String,
    var phoneNumberPrefix: String,
    var completePhoneNumber: String,
    @PrimaryKey(autoGenerate = false)
    var userAccountId: String,
    var activityId: String? = null,
    var profession: Int,
    var lastName: String? = null,
    var emailAddress: String? = null,
    var linkedInUsername: String? = null,
    var gitHubUsername: String? = null,
    var universityName: String? = null,
    var createdOn: Long? = null,
    var lastUpdatedOn: Long? = null,
)

object SingletonUserData {

    private var userProfile: UserProfile? = null
    fun getInstance() = userProfile

    fun setInstance(userProfile: UserProfile) {
        this.userProfile = userProfile
    }
}