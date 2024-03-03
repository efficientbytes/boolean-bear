package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.SignInToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationService {

    @POST("user/sign-in/")
    suspend fun getSignInToken(
        @Body phoneNumber: PhoneNumber
    ): Response<SignInToken>

}