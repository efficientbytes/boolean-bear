package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.ContentCategoriesStatus
import retrofit2.Response
import retrofit2.http.GET

interface AssetsService {

    @GET("content/categories")
    suspend fun getContentCategories(): Response<ContentCategoriesStatus>

}