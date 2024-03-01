package app.efficientbytes.androidnow.services.models

data class VerifyPhoneNumber(
    val phoneNumber : String,
    val otp : String?=null
)
