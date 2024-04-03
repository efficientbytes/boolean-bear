package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.ContentCategoriesStatus
import app.efficientbytes.booleanbear.services.models.ShuffledCategoryContentIds
import app.efficientbytes.booleanbear.services.models.YoutubeContentViewStatus
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
    ): Response<YoutubeContentViewStatus>

}