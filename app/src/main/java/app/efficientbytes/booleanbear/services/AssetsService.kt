package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.ContentCategoriesStatus
import app.efficientbytes.booleanbear.services.models.InstructorProfileStatus
import app.efficientbytes.booleanbear.services.models.PlayDetails
import app.efficientbytes.booleanbear.services.models.PlayUrl
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLinkStatus
import app.efficientbytes.booleanbear.services.models.ShuffledCategoryContentIds
import app.efficientbytes.booleanbear.services.models.ShuffledContentResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AssetsService {

    @GET("categories")
    suspend fun getCategories(
        @Query(value = "categoryType") categoryType: String = "shuffled"
    ): Response<ContentCategoriesStatus>

    @GET("categories/shuffled/{categoryId}")
    suspend fun getContentIdsUnderShuffledCategoryForCategoryId(
        @Path("categoryId") categoryId: String
    ): Response<ShuffledCategoryContentIds>

    @GET("contents/{contentId}")
    suspend fun getYoutubeTypeContentForContentId(
        @Path("contentId") contentId: String,
        @Query(value = "viewType") viewType: String = "YOUTUBE"
    ): Response<ShuffledContentResponse>

    @GET("contents/{contentId}/play-link")
    suspend fun getPlayUrl(
        @Path("contentId") contentId: String,
    ): Response<PlayUrl>

    @GET("contents/{contentId}/play-details")
    suspend fun getPlayDetails(
        @Path("contentId") contentId: String,
    ): Response<PlayDetails>

    @GET("instructors/profile/{instructorId}")
    suspend fun getInstructorDetails(
        @Path("instructorId") instructorId: String,
    ): Response<InstructorProfileStatus>

    @GET("mentioned-links/{linkId}")
    suspend fun getMentionedLinks(
        @Path("linkId") linkId: String,
    ): Response<RemoteMentionedLinkStatus>
}