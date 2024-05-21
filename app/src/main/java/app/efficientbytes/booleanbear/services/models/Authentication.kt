package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PasswordAuthenticationResponse(
    val data: PhoneNumber? = null,
    val message: String? = null
)