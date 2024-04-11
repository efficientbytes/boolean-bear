package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.models.LocalYoutubeContentView
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.models.CategoryType
import app.efficientbytes.booleanbear.models.ContentViewType
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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

    fun fetchCategories(categoryType: CategoryType) = flow {
        emit(DataStatus.loading())
        when (categoryType) {
            CategoryType.SHUFFLED -> {
                try {
                    val response = assetsService.getCategories(categoryType.categoryKey)
                    val responseCode = response.code()
                    when {
                        responseCode == 200 -> {
                            val body = response.body()
                            if (body != null) {
                                if (body.categoryList.isEmpty()) {
                                    emit(DataStatus.emptyResult<List<ServiceContentCategory>>())
                                } else {
                                    emit(DataStatus.success(body.categoryList))
                                }
                            } else {
                                emit(DataStatus.emptyResult<List<ServiceContentCategory>>())
                            }
                        }

                        responseCode >= 400 -> {
                            val errorResponse: ContentCategoriesStatus = gson.fromJson(
                                response.errorBody()!!.string(),
                                ContentCategoriesStatus::class.java
                            )
                            emit(DataStatus.failed<List<ServiceContentCategory>>(errorResponse.message))
                        }
                    }
                } catch (noInternet: NoInternetException) {
                    emit(DataStatus.noInternet<List<ServiceContentCategory>>())
                } catch (socketTimeOutException: SocketTimeoutException) {
                    emit(DataStatus.timeOut<List<ServiceContentCategory>>())
                } catch (exception: IOException) {
                    emit(DataStatus.unknownException<List<ServiceContentCategory>>(exception.message.toString()))
                }
            }

            CategoryType.CURATED -> {

            }
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun downloadShuffledCategories(categoryListener: CategoryListener) {
        externalScope.launch {
            fetchCategories(CategoryType.SHUFFLED).collect {
                when (it.status) {
                    DataStatus.Status.EmptyResult -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.emptyResult()
                    )

                    DataStatus.Status.Failed -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.failed(it.message.toString())
                    )

                    DataStatus.Status.Loading -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.loading()
                    )

                    DataStatus.Status.NoInternet -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.noInternet()
                    )

                    DataStatus.Status.Success -> {
                        it.data?.let { list ->
                            saveCategories(CategoryType.SHUFFLED, list)
                            val serviceContentCategory =
                                list.find { serviceContentCategory -> serviceContentCategory.index == 1 }
                            if (serviceContentCategory != null) {
                                categoryListener.onIndex1CategoryFound(serviceContentCategory.id)
                            }
                        }
                        categoryListener.onCategoryDataStatusChanged(DataStatus.success(true))
                    }

                    DataStatus.Status.TimeOut -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.timeOut()
                    )

                    DataStatus.Status.UnAuthorized -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.unAuthorized()
                    )

                    DataStatus.Status.UnKnownException -> categoryListener.onCategoryDataStatusChanged(
                        DataStatus.unknownException(
                            it.message.toString()
                        )
                    )
                }
            }
        }
    }

    private fun saveCategories(
        categoryType: CategoryType,
        contentCategories: List<ServiceContentCategory>
    ) {
        externalScope.launch {
            when (categoryType) {
                CategoryType.SHUFFLED -> {
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

                CategoryType.CURATED -> {

                }
            }
        }
    }

    fun fetchContent(contentId: String, viewType: ContentViewType) = flow {
        emit(DataStatus.loading())
        when (viewType) {
            ContentViewType.YOUTUBE -> {
                val result = assetsDao.getShuffledYoutubeViewContent(contentId)
                if (result != null) {
                    emit(DataStatus.success(result))
                } else {
                    try {
                        val response = assetsService.getYoutubeTypeContentForContentId(contentId)
                        val responseCode = response.code()
                        when {
                            responseCode == 200 -> {
                                val body = response.body()
                                if (body != null) {
                                    if (body.youtubeContentView == null) {
                                        emit(DataStatus.emptyResult<YoutubeContentView>())
                                    } else {
                                        emit(DataStatus.success(body.youtubeContentView))
                                    }
                                } else {
                                    emit(DataStatus.emptyResult<YoutubeContentView>())
                                }
                            }

                            responseCode >= 400 -> {
                                val errorResponse: ContentCategoriesStatus = gson.fromJson(
                                    response.errorBody()!!.string(),
                                    ContentCategoriesStatus::class.java
                                )
                                emit(DataStatus.failed<YoutubeContentView>(errorResponse.message))
                            }
                        }
                    } catch (noInternet: NoInternetException) {
                        emit(DataStatus.noInternet<YoutubeContentView>())
                    } catch (socketTimeOutException: SocketTimeoutException) {
                        emit(DataStatus.timeOut<YoutubeContentView>())
                    } catch (exception: IOException) {
                        emit(DataStatus.unknownException<YoutubeContentView>(exception.message.toString()))
                    }
                }
            }
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    private fun fetchAllContent(contentIdList: List<String>, viewType: ContentViewType) = flow {
        emit(DataStatus.loading())
        when (viewType) {
            ContentViewType.YOUTUBE -> {
                try {
                    val list = mutableListOf<YoutubeContentView>()
                    val jobs = contentIdList.map { id ->
                        externalScope.launch {
                            try {
                                val response =
                                    assetsService.getYoutubeTypeContentForContentId(id)
                                val responseCode = response.code()
                                when {
                                    responseCode == 200 -> {
                                        val youtubeViewContent =
                                            response.body()?.youtubeContentView
                                        youtubeViewContent?.let {
                                            list.add(it)
                                        }
                                    }

                                    responseCode >= 400 -> {
                                        val errorResponse: YoutubeContentViewStatus =
                                            gson.fromJson(
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
                    if (list.isEmpty()) {
                        emit(DataStatus.emptyResult<MutableList<YoutubeContentView>>())
                    } else {
                        emit(DataStatus.success<MutableList<YoutubeContentView>>(list))
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
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    private suspend fun fetchAllContentIds(
        categoryId: String,
        categoryType: CategoryType
    ): DataStatus<List<String>> {
        val result = when (categoryType) {
            CategoryType.SHUFFLED -> {
                val list = externalScope.async {
                    try {
                        val response =
                            assetsService.getContentIdsUnderShuffledCategoryForCategoryId(categoryId)
                        val responseCode = response.code()
                        val status = when {
                            responseCode == 200 -> {
                                val body = response.body()
                                if (body != null) {
                                    if (body.contentIds.isEmpty()) {
                                        DataStatus.emptyResult<List<String>>()
                                    } else {
                                        DataStatus.success<List<String>>(body.contentIds)
                                    }
                                } else {
                                    DataStatus.emptyResult<List<String>>()
                                }
                            }

                            responseCode >= 400 -> {
                                val errorResponse: ShuffledCategoryContentIds = gson.fromJson(
                                    response.errorBody()!!.string(),
                                    ShuffledCategoryContentIds::class.java
                                )
                                DataStatus.failed<List<String>>(errorResponse.message)
                            }

                            else -> {
                                DataStatus.unknownException<List<String>>("We encountered a problem while getting individual contents.")
                            }
                        }
                        return@async status
                    } catch (noInternet: NoInternetException) {
                        return@async DataStatus.noInternet<List<String>>()
                    } catch (socketTimeOutException: SocketTimeoutException) {
                        return@async DataStatus.timeOut<List<String>>()
                    } catch (exception: IOException) {
                        return@async DataStatus.unknownException<List<String>>(exception.message.toString())
                    }
                }

                list.await()
            }

            CategoryType.CURATED -> {
                DataStatus.success(emptyList())
            }
        }
        return result
    }

    private var allContentJob: Job? = null
    fun getAllContent(
        categoryId: String,
        categoryType: CategoryType,
        contentListener: ContentListener
    ) {
        if (allContentJob != null) {
            allContentJob?.cancel()
            allContentJob = null
        }
        allContentJob = externalScope.launch {
            if (categoryType === CategoryType.SHUFFLED) {
                contentListener.onContentsDataStatusChanged(DataStatus.loading())
                val listFromDB = assetsDao.getAllShuffledYoutubeViewContents(categoryId)
                if (listFromDB.isNotEmpty()) {
                    contentListener.onContentsDataStatusChanged(DataStatus.success(listFromDB))
                } else {
                    //fetch from server
                    val contentIds = fetchAllContentIds(categoryId, CategoryType.SHUFFLED)
                    when (contentIds.status) {
                        DataStatus.Status.EmptyResult -> contentListener.onContentsDataStatusChanged(
                            DataStatus.emptyResult()
                        )

                        DataStatus.Status.Failed -> contentListener.onContentsDataStatusChanged(
                            DataStatus.failed(contentIds.message.toString())
                        )

                        DataStatus.Status.Loading -> contentListener.onContentsDataStatusChanged(
                            DataStatus.loading()
                        )

                        DataStatus.Status.NoInternet -> contentListener.onContentsDataStatusChanged(
                            DataStatus.noInternet()
                        )

                        DataStatus.Status.Success -> {
                            val contentsIdsList = contentIds.data
                            if (contentsIdsList == null) {
                                contentListener.onContentsDataStatusChanged(DataStatus.emptyResult())
                                return@launch
                            } else {
                                fetchAllContent(
                                    contentsIdsList,
                                    ContentViewType.YOUTUBE
                                ).collect { youtubeViewContents ->
                                    when (youtubeViewContents.status) {
                                        DataStatus.Status.EmptyResult -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.emptyResult()
                                        )

                                        DataStatus.Status.Failed -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.failed(youtubeViewContents.message.toString())
                                        )

                                        DataStatus.Status.Loading -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.loading()
                                        )

                                        DataStatus.Status.NoInternet -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.noInternet()
                                        )

                                        DataStatus.Status.Success -> {
                                            val youtubeContentList = youtubeViewContents.data
                                            if (youtubeContentList == null) {
                                                contentListener.onContentsDataStatusChanged(
                                                    DataStatus.emptyResult()
                                                )
                                            } else {
                                                contentListener.onContentsDataStatusChanged(
                                                    youtubeViewContents
                                                )
                                                val modifiedList = youtubeContentList.map {
                                                    LocalYoutubeContentView(
                                                        categoryId,
                                                        it.contentId,
                                                        it.title,
                                                        it.instructorName,
                                                        it.createdOn,
                                                        it.runTime,
                                                        it.thumbnail
                                                    )
                                                }
                                                assetsDao.insertShuffledCategoryContents(
                                                    modifiedList
                                                )
                                            }
                                        }

                                        DataStatus.Status.TimeOut -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.timeOut()
                                        )

                                        DataStatus.Status.UnAuthorized -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.unAuthorized()
                                        )

                                        DataStatus.Status.UnKnownException -> contentListener.onContentsDataStatusChanged(
                                            DataStatus.unknownException(youtubeViewContents.message.toString())
                                        )
                                    }
                                }
                            }
                        }

                        DataStatus.Status.TimeOut -> contentListener.onContentsDataStatusChanged(
                            DataStatus.timeOut()
                        )

                        DataStatus.Status.UnAuthorized -> contentListener.onContentsDataStatusChanged(
                            DataStatus.unAuthorized()
                        )

                        DataStatus.Status.UnKnownException -> contentListener.onContentsDataStatusChanged(
                            DataStatus.unknownException(contentIds.message.toString())
                        )
                    }
                }
            }
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

    fun deleteAllContents() {
        externalScope.launch {
            assetsDao.deleteShuffledYoutubeContentView()
        }
    }

    interface CategoryListener {

        fun onCategoryDataStatusChanged(status: DataStatus<Boolean>)

        fun onIndex1CategoryFound(categoryId: String)

    }

    interface ContentListener {

        fun onContentsDataStatusChanged(status: DataStatus<List<YoutubeContentView>>)

    }

}