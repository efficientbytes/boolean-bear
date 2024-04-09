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
import app.efficientbytes.booleanbear.utils.NoInternetException
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class AssetsRepository(
    private val assetsService: AssetsService,
    private val assetsDao: AssetsDao,
    private val externalScope: CoroutineScope
) {

    private val gson = Gson()
    val categoriesFromDB: Flow<MutableList<ShuffledCategory>> =
        assetsDao.getShuffledCategories()

    suspend fun getShuffledCategories() = flow {
        try {
            emit(DataStatus.loading())
            val response = assetsService.getCategories()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val categoryList = response.body()
                    if (categoryList != null) {
                        if (categoryList.categoryList.isEmpty()) {
                            emit(DataStatus.emptyResult())
                        } else {
                            emit(DataStatus.success(categoryList))
                        }
                    }
                }

                responseCode >= 400 -> {
                    val errorResponse: ContentCategoriesStatus = gson.fromJson(
                        response.errorBody()!!.string(),
                        ContentCategoriesStatus::class.java
                    )
                    emit(DataStatus.failed(errorResponse.message))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch {
        emit(DataStatus.unknownException(it.message.toString()))
    }.flowOn(Dispatchers.IO)

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
        try {
            emit(DataStatus.loading())
            val response = assetsService.getContentIdsUnderShuffledCategoryForCategoryId(categoryId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val shuffledContentIds = response.body()
                    if (shuffledContentIds != null) {
                        if (shuffledContentIds.contentIds.isEmpty()) {
                            emit(DataStatus.emptyResult())
                        } else {
                            emit(DataStatus.success(shuffledContentIds))
                        }
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun getYoutubeTypeContentViewForContentId(contentId: String) = flow {
        try {
            emit(DataStatus.loading())
            val response = assetsService.getYoutubeTypeContentForContentId(contentId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val youtubeContentViewStatus = response.body()
                    if (youtubeContentViewStatus != null) {
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun getYoutubeTypeContentForListOf(contentIds: List<String>): List<YoutubeContentView> {
        val list = mutableListOf<YoutubeContentView>()
        try {
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
                    } catch (noInternet: NoInternetException) {
                        throw NoInternetException()
                    } catch (socketTimeOutException: SocketTimeoutException) {
                        throw SocketTimeoutException()
                    } catch (exception: IOException) {
                        throw IOException()
                    }
                }
            }
            jobs.joinAll()
            return list
        } catch (noInternet: NoInternetException) {
            throw NoInternetException()
        } catch (socketTimeOutException: SocketTimeoutException) {
            throw SocketTimeoutException()
        } catch (exception: IOException) {
            throw IOException()
        }
    }

    suspend fun getPlayUrl(contentId: String) = flow {
        try {
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch {
        emit(DataStatus.unknownException(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    suspend fun getPlayDetails(contentId: String) = flow {
        try {
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch {
        emit(DataStatus.unknownException(it.message.toString()))
    }.flowOn(Dispatchers.IO)

}