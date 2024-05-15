package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.UserProfileResponse
import app.efficientbytes.booleanbear.services.models.WaitingListCoursesResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserProfileService {

    @FormUrlEncoded
    @POST("user/profile/update/basic-details")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun updateUserPrivateProfileBasicDetails(
        @Field("firstName") firstName: String? = null,
        @Field("phoneNumber") phoneNumber: String,
        @Field("phoneNumberPrefix") phoneNumberPrefix: String,
        @Field("completePhoneNumber") completePhoneNumber: String,
        @Field("userAccountId") userAccountId: String,
        @Field("activityId") activityId: String? = null,
        @Field("profession") profession: Int? = 0,
        @Field("lastName") lastName: String? = null,
        @Field("emailAddress") emailAddress: String? = null,
        @Field("linkedInUsername") linkedInUsername: String? = null,
        @Field("gitHubUsername") gitHubUsername: String? = null,
        @Field("universityName") universityName: String? = null,
        @Field("createdOn") createdOn: Long? = null,
        @Field("lastUpdatedOn") lastUpdatedOn: Long? = null,
    ): Response<UserProfileResponse>

    @FormUrlEncoded
    @POST("user/profile/update")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun updateUserPrivateProfile(
        @Field("firstName") firstName: String? = null,
        @Field("phoneNumber") phoneNumber: String,
        @Field("phoneNumberPrefix") phoneNumberPrefix: String,
        @Field("completePhoneNumber") completePhoneNumber: String,
        @Field("userAccountId") userAccountId: String,
        @Field("activityId") activityId: String? = null,
        @Field("profession") profession: Int? = 0,
        @Field("lastName") lastName: String? = null,
        @Field("emailAddress") emailAddress: String? = null,
        @Field("linkedInUsername") linkedInUsername: String? = null,
        @Field("gitHubUsername") gitHubUsername: String? = null,
        @Field("universityName") universityName: String? = null,
        @Field("createdOn") createdOn: Long? = null,
        @Field("lastUpdatedOn") lastUpdatedOn: Long? = null,
    ): Response<UserProfileResponse>

    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @FormUrlEncoded
    @POST("user/notifications/token/upload")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun uploadNotificationsToken(
        @Field("token") token: String
    ): Response<ResponseMessage>

    @FormUrlEncoded
    @POST("user/notification/token/delete")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun deleteFCMToken(): Response<ResponseMessage>

    @GET("user/courses/waiting-list")
    suspend fun getWaitingListCourses(): Response<WaitingListCoursesResponse>


}