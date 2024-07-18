package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.InstructorProfileResponse
import app.efficientbytes.booleanbear.services.models.ReelDetailsResponse
import app.efficientbytes.booleanbear.services.models.ReelPlayLink
import app.efficientbytes.booleanbear.services.models.ReelResponse
import app.efficientbytes.booleanbear.services.models.ReelTopicResponse
import app.efficientbytes.booleanbear.services.models.ReelTopicsResponse
import app.efficientbytes.booleanbear.services.models.ReelVideoIdResponse
import app.efficientbytes.booleanbear.services.models.ReelsResponse
import app.efficientbytes.booleanbear.services.models.RemoteCourseBundleResponse
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLinkResponse
import app.efficientbytes.booleanbear.services.models.WaitingListCourseResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AssetsService {

    @GET("reels/topics")
    suspend fun getReelTopics(): Response<ReelTopicsResponse>

    @GET("reels/topics/{topicId}/topic-details")
    suspend fun getReelTopicDetails(
        @Path("topicId") topicId: String
    ): Response<ReelTopicResponse>

    @GET("reels/topics/{topicId}")
    suspend fun getReels(
        @Path("topicId") topicId: String
    ): Response<ReelsResponse>

    @GET("reels/{reelId}/reel-details")
    suspend fun getReel(
        @Path("reelId") reelId: String,
        @Query("in_detailed") detailed: Int = 0
    ): Response<ReelResponse>

    @GET("reels/{reelId}/reel-details")
    suspend fun getReelDetails(
        @Path("reelId") reelId: String,
        @Query("in_detailed") detailed: Int = 1
    ): Response<ReelDetailsResponse>

    @GET("reels/{reelId}/video-id")
    suspend fun getReelVideoId(
        @Path("reelId") reelId: String
    ): Response<ReelVideoIdResponse>

    @GET("reels/{videoId}/play-link")
    suspend fun getReelPlayLink(
        @Path("videoId") videoId: String
    ): Response<ReelPlayLink>

    @GET("instructors/profile/{instructorId}")
    suspend fun getInstructorDetails(
        @Path("instructorId") instructorId: String,
    ): Response<InstructorProfileResponse>

    @GET("mentioned-links/{linkId}")
    suspend fun getMentionedLinks(
        @Path("linkId") linkId: String,
    ): Response<RemoteMentionedLinkResponse>

    @GET("courses/course-bundle")
    suspend fun getCourseBundle(): Response<RemoteCourseBundleResponse>

    @POST("courses/{courseId}/join-waiting-list")
    @FormUrlEncoded
    suspend fun joinCourseWaitingList(
        @Field("dummy") dummy: String = "",
        @Path("courseId") courseId: String
    ): Response<WaitingListCourseResponse>
}