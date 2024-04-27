package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalShuffledContent
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.models.CategoryType
import app.efficientbytes.booleanbear.models.ContentViewType
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.models.InstructorProfileResponse
import app.efficientbytes.booleanbear.services.models.PlayDetailsResponse
import app.efficientbytes.booleanbear.services.models.PlayUrl
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLinkResponse
import app.efficientbytes.booleanbear.services.models.RemoteShuffledCategory
import app.efficientbytes.booleanbear.services.models.RemoteShuffledContent
import app.efficientbytes.booleanbear.services.models.ShuffledCategoriesResponse
import app.efficientbytes.booleanbear.services.models.ShuffledCategoryContentIdListResponse
import app.efficientbytes.booleanbear.services.models.ShuffledContentResponse
import app.efficientbytes.booleanbear.utils.NoInternetException
import app.efficientbytes.booleanbear.utils.sanitizeSearchQuery
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
                                if (body.data.isEmpty()) {
                                    emit(DataStatus.emptyResult<List<RemoteShuffledCategory>>())
                                } else {
                                    emit(DataStatus.success(body.data))
                                }
                            } else {
                                emit(DataStatus.emptyResult<List<RemoteShuffledCategory>>())
                            }
                        }

                        responseCode >= 400 -> {
                            val errorResponse: ShuffledCategoriesResponse = gson.fromJson(
                                response.errorBody()!!.string(),
                                ShuffledCategoriesResponse::class.java
                            )
                            emit(DataStatus.failed<List<RemoteShuffledCategory>>(errorResponse.message))
                        }
                    }
                } catch (noInternet: NoInternetException) {
                    emit(DataStatus.noInternet<List<RemoteShuffledCategory>>())
                } catch (socketTimeOutException: SocketTimeoutException) {
                    emit(DataStatus.timeOut<List<RemoteShuffledCategory>>())
                } catch (exception: IOException) {
                    emit(DataStatus.unknownException<List<RemoteShuffledCategory>>(exception.message.toString()))
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
        contentCategories: List<RemoteShuffledCategory>
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
                                    if (body.data == null) {
                                        emit(DataStatus.emptyResult<RemoteShuffledContent>())
                                    } else {
                                        emit(DataStatus.success(body.data))
                                    }
                                } else {
                                    emit(DataStatus.emptyResult<RemoteShuffledContent>())
                                }
                            }

                            responseCode >= 400 -> {
                                val errorResponse: ShuffledCategoriesResponse = gson.fromJson(
                                    response.errorBody()!!.string(),
                                    ShuffledCategoriesResponse::class.java
                                )
                                emit(DataStatus.failed<RemoteShuffledContent>(errorResponse.message))
                            }
                        }
                    } catch (noInternet: NoInternetException) {
                        emit(DataStatus.noInternet<RemoteShuffledContent>())
                    } catch (socketTimeOutException: SocketTimeoutException) {
                        emit(DataStatus.timeOut<RemoteShuffledContent>())
                    } catch (exception: IOException) {
                        emit(DataStatus.unknownException<RemoteShuffledContent>(exception.message.toString()))
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
                    val list = mutableListOf<RemoteShuffledContent>()
                    val jobs = contentIdList.map { id ->
                        externalScope.launch {
                            try {
                                val response =
                                    assetsService.getYoutubeTypeContentForContentId(id)
                                val responseCode = response.code()
                                when {
                                    responseCode == 200 -> {
                                        val youtubeViewContent =
                                            response.body()?.data
                                        youtubeViewContent?.let {
                                            list.add(it)
                                        }
                                    }

                                    responseCode >= 400 -> {
                                        val errorResponse: ShuffledContentResponse =
                                            gson.fromJson(
                                                response.errorBody()!!.string(),
                                                ShuffledContentResponse::class.java
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
                        emit(DataStatus.emptyResult<MutableList<RemoteShuffledContent>>())
                    } else {
                        emit(DataStatus.success<MutableList<RemoteShuffledContent>>(list))
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
                                    if (body.data.isEmpty()) {
                                        DataStatus.emptyResult<List<String>>()
                                    } else {
                                        DataStatus.success<List<String>>(body.data)
                                    }
                                } else {
                                    DataStatus.emptyResult<List<String>>()
                                }
                            }

                            responseCode >= 400 -> {
                                val errorResponse: ShuffledCategoryContentIdListResponse =
                                    gson.fromJson(
                                        response.errorBody()!!.string(),
                                        ShuffledCategoryContentIdListResponse::class.java
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
                if (!listFromDB.isNullOrEmpty()) {
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
                                                    LocalShuffledContent(
                                                        categoryId,
                                                        it.contentId,
                                                        it.title,
                                                        it.instructorName,
                                                        it.createdOn,
                                                        it.runTime,
                                                        it.thumbnail,
                                                        it.hashTags
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

                responseCode == 404 -> {
                    emit(DataStatus.emptyResult())
                }

                responseCode >= 400 && responseCode != 404 -> {
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
                    val playDetailsResponse = response.body()
                    if (playDetailsResponse != null) {
                        val playDetails = playDetailsResponse.data
                        if (playDetails != null) {
                            emit(DataStatus.success(playDetails))
                        } else {
                            emit(DataStatus.emptyResult())
                        }
                    } else {
                        emit(DataStatus.emptyResult())
                    }
                }

                responseCode == 404 -> {
                    emit(DataStatus.emptyResult())
                }

                responseCode >= 400 && responseCode != 404 -> {
                    val errorResponse: PlayDetailsResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        PlayDetailsResponse::class.java
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

    private fun fetchInstructorDetails(instructorId: String) = flow {
        emit(DataStatus.loading<RemoteInstructorProfile>())
        try {
            val response = assetsService.getInstructorDetails(instructorId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val instructorProfileStatus = response.body()
                    if (instructorProfileStatus == null) {
                        emit(DataStatus.emptyResult<RemoteInstructorProfile>())
                    } else {
                        val instructorProfile = instructorProfileStatus.data
                        if (instructorProfile == null) {
                            emit(DataStatus.emptyResult<RemoteInstructorProfile>())
                        } else {
                            emit(DataStatus.success<RemoteInstructorProfile>(instructorProfile))
                        }
                    }
                }

                responseCode >= 400 -> {
                    val errorResponse: InstructorProfileResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        InstructorProfileResponse::class.java
                    )
                    emit(DataStatus.failed<RemoteInstructorProfile>(errorResponse.message.toString()))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }

    fun getInstructorDetails(
        instructorId: String,
        instructorProfileListener: InstructorProfileListener
    ) {
        externalScope.launch {
            val result = assetsDao.getInstructorProfile(instructorId)
            if (result != null) {
                instructorProfileListener.onInstructorProfileDataStatusChanged(
                    DataStatus.success(
                        result
                    )
                )
            } else {
                fetchInstructorDetails(instructorId).collect {
                    when (it.status) {
                        DataStatus.Status.EmptyResult -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.emptyResult()
                        )

                        DataStatus.Status.Failed -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.failed(it.message.toString())
                        )

                        DataStatus.Status.Loading -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.loading()
                        )

                        DataStatus.Status.NoInternet -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.noInternet()
                        )

                        DataStatus.Status.Success -> {
                            val instructorProfile = it.data
                            instructorProfile?.let { profile ->
                                instructorProfileListener.onInstructorProfileDataStatusChanged(
                                    DataStatus.success(profile)
                                )
                                val skillList = profile.skills ?: emptyList<String>()
                                val localInstructorProfile = LocalInstructorProfile(
                                    instructorId = profile.instructorId,
                                    firstName = profile.firstName,
                                    lastName = profile.lastName,
                                    bio = profile.bio,
                                    oneLineDescription = profile.oneLineDescription,
                                    profession = profile.profession,
                                    workingAt = profile.workingAt,
                                    profileImage = profile.profileImage,
                                    coverImage = profile.coverImage,
                                    gitHubUsername = profile.gitHubUsername,
                                    linkedInUsername = profile.linkedInUsername,
                                    skills = skillList
                                )
                                assetsDao.insertInstructorProfile(localInstructorProfile)
                            }
                        }

                        DataStatus.Status.TimeOut -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.timeOut()
                        )

                        DataStatus.Status.UnAuthorized -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.unAuthorized()
                        )

                        DataStatus.Status.UnKnownException -> instructorProfileListener.onInstructorProfileDataStatusChanged(
                            DataStatus.unknownException(it.message.toString())
                        )
                    }
                }
            }
        }
    }

    fun deleteAllInstructorDetails() {
        externalScope.launch {
            assetsDao.deleteAllInstructorProfile()
        }
    }

    suspend fun fetchMentionedLink(linkId: String) = flow {
        try {
            val response = assetsService.getMentionedLinks(linkId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val mentionedLinkStatus = response.body()
                    if (mentionedLinkStatus != null) {
                        val mentionedLink = mentionedLinkStatus.data
                        if (mentionedLink != null) {
                            emit(DataStatus.success<RemoteMentionedLink>(mentionedLink))
                        } else emit(DataStatus.emptyResult<RemoteMentionedLink>())
                    } else {
                        emit(DataStatus.emptyResult<RemoteMentionedLink>())
                    }
                }

                responseCode >= 400 -> {
                    val errorResponse: RemoteMentionedLinkResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        RemoteMentionedLinkResponse::class.java
                    )
                    emit(DataStatus.failed<RemoteMentionedLink>(errorResponse.message.toString()))
                }

                else -> {
                    emit(DataStatus.emptyResult<RemoteMentionedLink>())
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet<RemoteMentionedLink>())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut<RemoteMentionedLink>())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException<RemoteMentionedLink>(exception.message.toString()))
        }
    }.catch {
        emit(DataStatus.unknownException(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    suspend fun getMentionedLink(linkId: String) = flow {
        val result = assetsDao.getMentionedLink(linkId)
        if (result != null) {
            emit(DataStatus.success(result))
        } else {
            fetchMentionedLink(linkId).collect {
                emit(it)
                when (it.status) {
                    DataStatus.Status.Success -> {
                        val link = it.data
                        link?.let { li ->
                            assetsDao.insertMentionedLink(
                                LocalMentionedLink(
                                    linkId = li.linkId,
                                    link = li.link,
                                    name = li.name,
                                    createdOn = li.createdOn
                                )
                            )
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun getAllMentionedLinks(list: List<String>, mentionedLinksListener: MentionedLinksListener) {
        externalScope.launch {
            mentionedLinksListener.onMentionedLinkDataStatusChanged(DataStatus.loading())
            try {
                val result = mutableListOf<RemoteMentionedLink>()
                val jobs = list.map { id ->
                    externalScope.launch {
                        getMentionedLink(id).collect {
                            when (it.status) {
                                DataStatus.Status.Success -> {
                                    it.data?.let { link -> result.add(link) }
                                }

                                else -> {

                                }
                            }
                        }
                    }
                }
                jobs.joinAll()
                if (result.isEmpty()) {
                    mentionedLinksListener.onMentionedLinkDataStatusChanged(DataStatus.emptyResult())
                } else {
                    mentionedLinksListener.onMentionedLinkDataStatusChanged(
                        DataStatus.success(
                            result
                        )
                    )
                }
            } catch (noInternet: NoInternetException) {
                mentionedLinksListener.onMentionedLinkDataStatusChanged(DataStatus.noInternet())
            } catch (socketTimeOutException: SocketTimeoutException) {
                mentionedLinksListener.onMentionedLinkDataStatusChanged(DataStatus.timeOut())
            } catch (exception: IOException) {
                mentionedLinksListener.onMentionedLinkDataStatusChanged(
                    DataStatus.unknownException(
                        exception.message.toString()
                    )
                )
            }
        }
    }

    fun deleteAllMentionedLinks() {
        externalScope.launch {
            assetsDao.deleteAllMentionedLinks()
        }
    }

    suspend fun getSearchContents(
        categoryId: String,
        query: String? = null
    ): List<RemoteShuffledContent>? {
        return when {
            query.isNullOrEmpty() -> {
                assetsDao.getAllShuffledYoutubeViewContents(categoryId)
            }

            query.isNotEmpty() && query.startsWith("#") -> {
                val searchQuery = sanitizeSearchQuery(query.trim())
                assetsDao.getShuffledContentsByHashTags(categoryId, searchQuery)
            }

            query.isNotEmpty() && (!query.startsWith("#")) -> {
                val searchQuery = sanitizeSearchQuery(query.trim())
                assetsDao.getShuffledContentsByTitle(
                    categoryId,
                    searchQuery
                )
            }

            else -> {
                emptyList<RemoteShuffledContent>()
            }
        }
    }

    interface CategoryListener {

        fun onCategoryDataStatusChanged(status: DataStatus<Boolean>)

        fun onIndex1CategoryFound(categoryId: String)

    }

    interface ContentListener {

        fun onContentsDataStatusChanged(status: DataStatus<List<RemoteShuffledContent>>)

    }

    interface InstructorProfileListener {

        fun onInstructorProfileDataStatusChanged(status: DataStatus<RemoteInstructorProfile>)

    }

    interface MentionedLinksListener {

        fun onMentionedLinkDataStatusChanged(status: DataStatus<List<RemoteMentionedLink>>)

    }

}