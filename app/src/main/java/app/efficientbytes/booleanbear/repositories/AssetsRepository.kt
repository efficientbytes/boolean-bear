package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.models.ContentCategoriesStatus
import app.efficientbytes.booleanbear.services.models.PlayDetails
import app.efficientbytes.booleanbear.services.models.PlayUrl
import app.efficientbytes.booleanbear.services.models.ServiceContentCategory
import app.efficientbytes.booleanbear.services.models.ShuffledCategoryContentIds
import app.efficientbytes.booleanbear.services.models.YoutubeContentView
import app.efficientbytes.booleanbear.services.models.YoutubeContentViewStatus
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class AssetsRepository(
    private val assetsService: AssetsService,
    private val assetsDao: AssetsDao,
    private val externalScope: CoroutineScope
) {

    private val gson = Gson()
    val categoriesFromDB: Flow<MutableList<ShuffledCategory>> =
        assetsDao.getShuffledCategories()

    suspend fun getShuffledCategories() = flow {
        emit(DataStatus.loading())
        val response = assetsService.getCategories()
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
                emit(DataStatus.failed(errorResponse.message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveShuffledCategories(contentCategories: List<ServiceContentCategory>) {
        assetsDao.insertShuffledCategories(contentCategories.map { contentCategory ->
            ShuffledCategory(
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

    suspend fun getContentIdsUnderShuffledCategoryForCategoryId(categoryId: String) = flow {
        emit(DataStatus.loading())
        val response = assetsService.getContentIdsUnderShuffledCategoryForCategoryId(categoryId)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val shuffledContentIds = response.body()
                if (shuffledContentIds?.contentIds?.isEmpty() == true) {
                    emit(DataStatus.failed("No search results"))
                } else {
                    emit(DataStatus.success(shuffledContentIds))
                }
            }

            responseCode >= 400 -> {
                val errorResponse: ShuffledCategoryContentIds = gson.fromJson(
                    response.errorBody()!!.string(),
                    ShuffledCategoryContentIds::class.java
                )
                emit(DataStatus.failed(errorResponse.message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun getYoutubeTypeContentViewForContentId(contentId: String) = flow {
        emit(DataStatus.loading())
        val response = assetsService.getYoutubeTypeContentForContentId(contentId)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val youtubeContentViewStatus = response.body()
                if (youtubeContentViewStatus?.youtubeContentView != null) {
                    emit(DataStatus.success(youtubeContentViewStatus.youtubeContentView))
                }
            }

            responseCode >= 400 -> {
                val errorResponse: YoutubeContentViewStatus = gson.fromJson(
                    response.errorBody()!!.string(),
                    YoutubeContentViewStatus::class.java
                )
                emit(DataStatus.failed(errorResponse.message.toString()))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun getYoutubeTypeContentForListOf(contentIds: List<String>): List<YoutubeContentView> {
        val list = mutableListOf<YoutubeContentView>()
        val jobs = contentIds.map { id ->
            externalScope.launch {
                try {
                    val response = assetsService.getYoutubeTypeContentForContentId(id)
                    val responseCode = response.code()
                    when {
                        responseCode == 200 -> {
                            val youtubeViewContent = response.body()?.youtubeContentView
                            youtubeViewContent?.let {
                                list.add(it)
                            }
                        }

                        responseCode >= 400 -> {
                            val errorResponse: YoutubeContentViewStatus = gson.fromJson(
                                response.errorBody()!!.string(),
                                YoutubeContentViewStatus::class.java
                            )
                        }
                    }
                } catch (exception: Exception) {
                }
            }
        }
        jobs.joinAll()
        return list
    }

    suspend fun getPlayUrl(contentId: String) = flow {
        emit(DataStatus.loading())
        val response = assetsService.getPlayUrl(contentId)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val playUrl = response.body()
                emit(DataStatus.success(playUrl))
            }

            responseCode >= 400 -> {
                val errorResponse: PlayUrl = gson.fromJson(
                    response.errorBody()!!.string(),
                    PlayUrl::class.java
                )
                emit(DataStatus.failed(errorResponse.message.toString()))
            }
        }
    }.catch {
        emit(DataStatus.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    suspend fun getPlayDetails(contentId: String) = flow {
        emit(DataStatus.loading())
        val response = assetsService.getPlayDetails(contentId)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val playDetails = response.body()
                emit(DataStatus.success(playDetails))
            }

            responseCode >= 400 -> {
                val errorResponse: PlayDetails = gson.fromJson(
                    response.errorBody()!!.string(),
                    PlayDetails::class.java
                )
                emit(DataStatus.failed(errorResponse.message.toString()))
            }
        }
    }.catch {
        emit(DataStatus.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

}