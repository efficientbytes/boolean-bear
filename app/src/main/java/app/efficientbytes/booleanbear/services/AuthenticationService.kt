package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.models.SingleDeviceLoginResponse
import app.efficientbytes.booleanbear.services.models.DeleteUserAccount
import app.efficientbytes.booleanbear.services.models.DeleteUserAccountStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import app.efficientbytes.booleanbear.services.models.SignInToken
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
    ): Response<SingleDeviceLoginResponse>

    @POST("user/delete-account")
    suspend fun deleteUserAccount(
        @Body deleteUserAccount: DeleteUserAccount
    ): Response<DeleteUserAccountStatus>

}