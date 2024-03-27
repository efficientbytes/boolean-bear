package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.models.SingleDeviceLogin
import app.efficientbytes.androidnow.services.models.DeleteUserAccount
import app.efficientbytes.androidnow.services.models.DeleteUserAccountStatus
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.SignInToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthenticationService {

    @POST("user/sign-in/")
    suspend fun getSignInToken(
        @Body phoneNumber: PhoneNumber
    ): Response<SignInToken>

    @GET("user/single-device-login")
    suspend fun getSingleDeviceLogin(
        @Query("userAccountId") userAccountId: String
    ): Response<SingleDeviceLogin>

    @POST("user/delete-account")
    suspend fun deleteUserAccount(
        @Body deleteUserAccount: DeleteUserAccount
    ): Response<DeleteUserAccountStatus>

}