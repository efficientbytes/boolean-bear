package app.efficientbytes.booleanbear.repositories

import android.util.Log
import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.models.ContentCategory
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.models.ContentCategoriesStatus
import app.efficientbytes.booleanbear.services.models.ServiceContentCategory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AssetsRepository(
    private val assetsService: AssetsService,
    private val assetsDao: AssetsDao
) {

    private val tagAssetsRepository = "Assets-Repository"
    private val gson = Gson()
    val contentCategoriesFromDB: Flow<MutableList<ContentCategory>> =
        assetsDao.getContentCategories()

    suspend fun getContentCategories() = flow {
        emit(DataStatus.loading())
        val response = assetsService.getContentCategories()
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val categoryList = response.body()
                emit(DataStatus.success(categoryList))
            }

            responseCode >= 400 -> {
                val errorResponse: ContentCategoriesStatus = gson.fromJson(
                    response.errorBody()!!.string(),
                    ContentCategoriesStatus::class.java
                )
                Log.i(tagAssetsRepository,"error is $errorResponse")
                emit(DataStatus.failed(errorResponse.message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveContentCategories(contentCategories: List<ServiceContentCategory>) {
        assetsDao.insertContentCategories(contentCategories.map { contentCategory ->
            ContentCategory(
                id = contentCategory.id,
                index = contentCategory.index,
                title = contentCategory.title,
                caption = contentCategory.caption,
                contentCount = contentCategory.contentCount,
                deepLink = contentCategory.deepLink,
                type1Thumbnail = contentCategory.type1Thumbnail,
                dateCreated = contentCategory.dateCreated,
                dateModified = contentCategory.dateModified
            )
        })
    }

}