package app.efficientbytes.androidnow.services.models

data class PhoneNumberVerificationStatus(
    val phoneNumber: String?=null,
    val verificationMessage: String?=null,
    val verificationStatus: String?=null
)