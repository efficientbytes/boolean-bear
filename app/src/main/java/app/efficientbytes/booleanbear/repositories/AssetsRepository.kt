package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.models.LocalCourse
import app.efficientbytes.booleanbear.database.models.LocalCourseTopic
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalReel
import app.efficientbytes.booleanbear.database.models.LocalReelTopic
import app.efficientbytes.booleanbear.database.models.LocalWaitingListCourse
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.models.InstructorProfileResponse
import app.efficientbytes.booleanbear.services.models.ReelDetailsResponse
import app.efficientbytes.booleanbear.services.models.ReelPlayLink
import app.efficientbytes.booleanbear.services.models.ReelTopicResponse
import app.efficientbytes.booleanbear.services.models.ReelTopicsResponse
import app.efficientbytes.booleanbear.services.models.ReelVideoIdResponse
import app.efficientbytes.booleanbear.services.models.ReelsResponse
import app.efficientbytes.booleanbear.services.models.RemoteCourseBundle
import app.efficientbytes.booleanbear.services.models.RemoteCourseBundleResponse
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLinkResponse
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.services.models.WaitingListCourseResponse
import app.efficientbytes.booleanbear.utils.NoInternetException
import app.efficientbytes.booleanbear.utils.sanitizeSearchQuery
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    fun getReelTopics() = flow {
        emit(DataStatus.loading())
        val result = assetsDao.getReelTopics()
        if (!result.isNullOrEmpty()) {
            emit(DataStatus.success(result))
        } else {
            try {
                val response = assetsService.getReelTopics()
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val body = response.body()
                        if (body != null) {
                            val reelTopics = body.data
                            if (reelTopics != null) {
                                if (reelTopics.isEmpty()) {
                                    emit(DataStatus.emptyResult())
                                } else {
                                    val sortedList = reelTopics.sortedBy { it.displayIndex }
                                    emit(DataStatus.success(sortedList))
                                    val modifiedList = reelTopics.map {
                                        LocalReelTopic(
                                            it.topicId,
                                            it.topic,
                                            it.displayIndex,
                                            it.type1Thumbnail
                                        )
                                    }
                                    assetsDao.insertReelTopics(modifiedList)
                                }
                            }
                        }
                    }

                    responseCode in 414..417 -> {
                        emit(DataStatus.unAuthorized(responseCode.toString()))
                    }

                    responseCode >= 400 -> {
                        val errorResponse: ReelTopicsResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ReelTopicsResponse::class.java
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
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getReelTopicDetails(topicId: String) = flow {
        emit(DataStatus.loading())
        val result = assetsDao.getReelTopicDetails(topicId)
        if (result != null) {
            emit(DataStatus.success(result))
        } else {
            try {
                val response = assetsService.getReelTopicDetails(topicId)
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val body = response.body()
                        if (body != null) {
                            val reelTopics = body.data
                            if (reelTopics != null) {
                                emit(DataStatus.success(reelTopics))
                            }
                        }
                    }

                    responseCode in 414..417 -> {
                        emit(DataStatus.unAuthorized(responseCode.toString()))
                    }

                    responseCode >= 400 -> {
                        val errorResponse: ReelTopicResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ReelTopicResponse::class.java
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
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getReels(topicId: String) = flow {
        emit(DataStatus.loading())
        val result = assetsDao.getReels(topicId)
        if (!result.isNullOrEmpty()) {
            emit(DataStatus.success(result))
        } else {
            try {
                val response = assetsService.getReels(topicId)
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val body = response.body()
                        if (body != null) {
                            val reels = body.data
                            if (reels != null) {
                                if (reels.isEmpty()) {
                                    emit(DataStatus.emptyResult())
                                } else {
                                    emit(DataStatus.success(reels))
                                    val nonNullTopicIdList =
                                        reels.filter { remoteReel -> remoteReel.topicId != null }
                                    val toLocalList = nonNullTopicIdList.map {
                                        LocalReel(
                                            it.topicId!!,
                                            it.reelId,
                                            it.title,
                                            it.instructorName,
                                            it.createdOn,
                                            it.runTime,
                                            it.thumbnail,
                                            it.hashTags
                                        )
                                    }
                                    assetsDao.insertReels(toLocalList)
                                }
                            }
                        }
                    }

                    responseCode in 414..417 -> {
                        emit(DataStatus.unAuthorized(responseCode.toString()))
                    }

                    responseCode >= 400 -> {
                        val errorResponse: ReelsResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ReelsResponse::class.java
                        )
                        emit(DataStatus.failed<List<RemoteReel>>(errorResponse.message.toString()))
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
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getReel(reelId: String) = flow {
        emit(DataStatus.loading())
        val result = assetsDao.getReel(reelId)
        if (result != null) {
            emit(DataStatus.success(result))
        } else {
            try {
                val response = assetsService.getReel(reelId)
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val body = response.body()
                        if (body != null) {
                            val reel = body.data
                            if (reel != null) {
                                emit(DataStatus.success(reel))
                            }
                        }
                    }

                    responseCode in 414..417 -> {
                        emit(DataStatus.unAuthorized(responseCode.toString()))
                    }

                    responseCode >= 400 -> {
                        val errorResponse: ReelsResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ReelsResponse::class.java
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
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getReelVideoId(reelId: String) = flow {
        emit(DataStatus.loading())
        try {
            val response = assetsService.getReelVideoId(reelId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val videoIdResponse = body.data
                        if (videoIdResponse != null) {
                            emit(DataStatus.success(videoIdResponse.videoId))
                        } else {
                            emit(DataStatus.emptyResult())
                        }
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: ReelVideoIdResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        ReelVideoIdResponse::class.java
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
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getReelDetails(reelId: String) = flow {
        emit(DataStatus.loading())
        try {
            val response = assetsService.getReelDetails(reelId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val reel = body.data
                        if (reel != null) {
                            emit(DataStatus.success(reel))
                        }
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: ReelDetailsResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        ReelDetailsResponse::class.java
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
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    suspend fun getReelPlayLink(videoId: String) = flow {
        try {
            emit(DataStatus.loading())
            val response = assetsService.getReelPlayLink(videoId)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val playLinkResponse = response.body()
                    if (playLinkResponse != null) {
                        val playLink = playLinkResponse.data
                        if (playLink != null) {
                            emit(DataStatus.success(playLink))
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

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 && responseCode != 404 -> {
                    val errorResponse: ReelPlayLink = gson.fromJson(
                        response.errorBody()!!.string(),
                        ReelPlayLink::class.java
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

    fun deleteReels() {
        externalScope.launch {
            assetsDao.deleteReels()
        }
    }

    fun deleteReelTopics() {
        externalScope.launch {
            assetsDao.deleteReelTopics()
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

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
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
                            DataStatus.unAuthorized(it.message)
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

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
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

    suspend fun getReelQueries(
        topicId: String,
        query: String? = null
    ): List<RemoteReel>? {
        return when {
            query.isNullOrEmpty() -> {
                assetsDao.getReels(topicId)
            }

            query.isNotEmpty() && query.startsWith("#") -> {
                val searchQuery = sanitizeSearchQuery(query.trim())
                assetsDao.getReelsByHashTags(topicId, searchQuery)
            }

            query.isNotEmpty() && (!query.startsWith("#")) -> {
                val searchQuery = sanitizeSearchQuery(query.trim())
                assetsDao.getReelsByTitle(
                    topicId,
                    searchQuery
                )
            }

            else -> {
                emptyList<RemoteReel>()
            }
        }
    }

    suspend fun getCourseBundle() = flow {
        emit(DataStatus.loading())
        val courseTopicResult = assetsDao.getCourseTopics()
        if (!courseTopicResult.isNullOrEmpty()) {
            val courseBundle = ArrayList<RemoteCourseBundle>()
            courseTopicResult.forEach { courseTopic ->
                val topicId = courseTopic.topicId
                val courseList = assetsDao.getCourses(topicId)
                if (!courseList.isNullOrEmpty()) {
                    courseBundle.add(RemoteCourseBundle(courseTopic, courseList))
                }
            }
            if (courseBundle.isNotEmpty()) emit(
                DataStatus.success(
                    courseBundle
                )
            )
        } else {
            try {
                val response = assetsService.getCourseBundle()
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val body = response.body()
                        if (body != null) {
                            val courseBundle = body.data
                            if (courseBundle != null) {
                                if (courseBundle.isEmpty()) {
                                    emit(DataStatus.emptyResult())
                                } else {
                                    emit(DataStatus.success(courseBundle.sortedBy { bundle -> bundle.topicDetails.displayIndex }))
                                    insertCourseBundle(courseBundle)
                                }
                            } else {
                                emit(DataStatus.emptyResult())
                            }
                        }
                    }

                    responseCode in 414..417 -> {
                        emit(DataStatus.unAuthorized(responseCode.toString()))
                    }

                    responseCode >= 400 -> {
                        val errorResponse: RemoteCourseBundleResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            RemoteCourseBundleResponse::class.java
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
        }
    }

    suspend fun insertCourseBundle(courseBundle: List<RemoteCourseBundle>) {
        externalScope.launch {
            courseBundle.forEach { remoteCourseBundles ->
                val remoteTopic = remoteCourseBundles.topicDetails
                val localTopic = LocalCourseTopic(
                    remoteTopic.topicId,
                    remoteTopic.topic,
                    remoteTopic.displayIndex,
                    remoteTopic.type1Thumbnail
                )
                assetsDao.insertCourseTopic(localTopic)
                val remoteCourses = remoteCourseBundles.courseList
                val localCourses = remoteCourses.map { remoteCourse ->
                    LocalCourse(
                        remoteCourse.courseId,
                        remoteCourse.title,
                        remoteCourse.type1Thumbnail,
                        remoteCourse.isAvailable,
                        remoteCourse.nonAvailabilityReason,
                        remoteCourse.hashTags,
                        remoteCourse.createdOn,
                        remoteCourse.topicId
                    )
                }
                assetsDao.insertCourses(localCourses)
            }
        }
    }

    fun deleteCourses() {
        externalScope.launch {
            assetsDao.deleteCourses()
        }
    }

    fun deleteCourseTopics() {
        externalScope.launch {
            assetsDao.deleteCourseTopics()
        }
    }

    fun joinCourseWaitingList(
        courseId: String,
        courseWaitingListListener: JoinCourseWaitingListListener? = null
    ) {
        externalScope.launch {
            courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                DataStatus.loading()
            )
            try {
                val response = assetsService.joinCourseWaitingList(courseId = courseId)
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        response.body()?.let {
                            courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                                DataStatus.success(true, it.message)
                            )
                            it.data
                        }?.let {
                            LocalWaitingListCourse(it.courseId)
                        }?.let {
                            assetsDao.insertCourseWaitingList(it)
                        }
                    }

                    responseCode in 414..417 -> {
                        courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                            DataStatus.unAuthorized(responseCode.toString())
                        )
                    }

                    responseCode >= 400 -> {
                        val errorResponse: WaitingListCourseResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            WaitingListCourseResponse::class.java
                        )
                        courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                            DataStatus.failed(errorResponse.message.toString())
                        )
                    }
                }
            } catch (noInternet: NoInternetException) {
                courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                    DataStatus.noInternet()
                )
            } catch (socketTimeOutException: SocketTimeoutException) {
                courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                    DataStatus.timeOut()
                )
            } catch (exception: IOException) {
                courseWaitingListListener?.onJoinCourseWaitingListDataStatusChanged(
                    DataStatus.unknownException(exception.message.toString())
                )
            }
        }
    }

    fun userJoinedWaitingList(courseId: String): Boolean {
        return assetsDao.userHasJoinedWaitingList(courseId)
    }

    suspend fun deleteCourseWaitingList() {
        assetsDao.deleteCourseWaitingList()
    }

    interface InstructorProfileListener {

        fun onInstructorProfileDataStatusChanged(status: DataStatus<RemoteInstructorProfile>)

    }

    interface MentionedLinksListener {

        fun onMentionedLinkDataStatusChanged(status: DataStatus<List<RemoteMentionedLink>>)

    }

    interface JoinCourseWaitingListListener {

        fun onJoinCourseWaitingListDataStatusChanged(status: DataStatus<Boolean>)

    }

}