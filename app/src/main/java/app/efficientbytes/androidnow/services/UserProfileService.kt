package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.services.models.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserProfileService {

    @POST("user-profile/update")
    suspend fun updateUserProfile(
        @Body userProfile: UserProfile
    ): Response<UserProfile>

    @GET("user-profile/search")
    suspend fun getUserProfile(
        @Query("phoneNumber") phoneNumber: String? = null,
        @Query("userAccountId") userAccountId: String? = null
    ): Response<UserProfile>

}