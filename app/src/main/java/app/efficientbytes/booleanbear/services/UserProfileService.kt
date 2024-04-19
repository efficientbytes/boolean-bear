package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.services.models.NotificationTokenStatus
import app.efficientbytes.booleanbear.services.models.RemoteNotificationToken
import app.efficientbytes.booleanbear.services.models.UserProfilePayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserProfileService {

    @POST("user/profile/update/basic-details")
    suspend fun updateUserPrivateProfileBasicDetails(
        @Body userProfile: UserProfile
    ): Response<UserProfilePayload>

    @POST("user/profile/update")
    suspend fun updateUserPrivateProfile(
        @Body userProfile: UserProfile
    ): Response<UserProfilePayload>

    @GET("/user/profile")
    suspend fun getUserProfile(
        @Query("userAccountId") userAccountId: String? = null
    ): Response<UserProfilePayload>

    @POST("user/notifications/token/upload")
    suspend fun uploadNotificationsToken(
        @Body remoteNotificationToken: RemoteNotificationToken
    ): Response<NotificationTokenStatus>

    @POST("user/notification/token/delete")
    suspend fun deleteFCMToken(
        @Body remoteNotificationToken: RemoteNotificationToken
    ): Response<NotificationTokenStatus>


}