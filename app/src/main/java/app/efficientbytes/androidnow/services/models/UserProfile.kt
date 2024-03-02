package app.efficientbytes.androidnow.services.models

data class UserProfile(
    val firstName: String,
    val phoneNumber: String,
    val phoneNumberPrefix: String,
    val completePhoneNumber: String,
    val profession: String,
    val userAccountId: String? = null,
    val fcmToken: String? = null,
    val createdOn: Long? = null,
    val lastName: String? = null,
    val emailAddress: String? = null,
    val linkedInAddress: String? = null,
    val universityName: String? = null
)
