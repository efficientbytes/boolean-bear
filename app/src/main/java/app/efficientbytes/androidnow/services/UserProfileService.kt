package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.services.models.UserProfilePayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserProfileService {

    @POST("user/profile/update/basic-details")
    suspend fun updateUserProfile(
        @Body userProfile: UserProfile
    ): Response<UserProfilePayload>

    @GET("/user/profile")
    suspend fun getUserProfile(
        @Query("userAccountId") userAccountId: String? = null
    ): Response<UserProfilePayload>

}