package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.models.SingleDeviceLoginResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.SignInTokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthenticationService {

    @FormUrlEncoded
    @POST("user/sign-in/")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun getSignInToken(
        @Field("phoneNumber") phoneNumber: String,
        @Field("prefix") prefix: String = "+91"
    ): Response<SignInTokenResponse>

    @GET("user/single-device-login")
    suspend fun getSingleDeviceLogin(): Response<SingleDeviceLoginResponse>

    @FormUrlEncoded
    @POST("user/delete-account")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun deleteUserAccount(
        @Field("userAccountId") userAccountId: String = ""
    ): Response<ResponseMessage>

    @FormUrlEncoded
    @POST("user/account/password/create")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun createAccountPassword(
        @Field("password") password: String
    ): Response<ResponseMessage>
}