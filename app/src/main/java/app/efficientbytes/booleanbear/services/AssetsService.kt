package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.InstructorProfileResponse
import app.efficientbytes.booleanbear.services.models.PlayDetailsResponse
import app.efficientbytes.booleanbear.services.models.PlayUrl
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLinkResponse
import app.efficientbytes.booleanbear.services.models.ShuffledCategoriesResponse
import app.efficientbytes.booleanbear.services.models.ShuffledCategoryContentIdListResponse
import app.efficientbytes.booleanbear.services.models.ShuffledContentResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AssetsService {

    @GET("categories")
    suspend fun getCategories(
        @Query(value = "categoryType") categoryType: String = "shuffled"
    ): Response<ShuffledCategoriesResponse>

    @GET("categories/shuffled/{categoryId}")
    suspend fun getContentIdsUnderShuffledCategoryForCategoryId(
        @Path("categoryId") categoryId: String
    ): Response<ShuffledCategoryContentIdListResponse>

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
    ): Response<PlayDetailsResponse>

    @GET("instructors/profile/{instructorId}")
    suspend fun getInstructorDetails(
        @Path("instructorId") instructorId: String,
    ): Response<InstructorProfileResponse>

    @GET("mentioned-links/{linkId}")
    suspend fun getMentionedLinks(
        @Path("linkId") linkId: String,
    ): Response<RemoteMentionedLinkResponse>
}