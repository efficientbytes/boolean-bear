package app.efficientbytes.booleanbear.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.USER_PROFILE_TABLE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = USER_PROFILE_TABLE)
data class UserProfile(
    var firstName: String? = null,
    var phoneNumber: String,
    var phoneNumberPrefix: String,
    var completePhoneNumber: String,
    @PrimaryKey(autoGenerate = false)
    var userAccountId: String,
    var activityId: String? = null,
    var profession: Int? = 0,
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

object SingletonPreviousUserId {

    private var userId: String? = null
    fun getInstance() = userId

    fun setInstance(userId: String?) {
        this.userId = userId
    }
}