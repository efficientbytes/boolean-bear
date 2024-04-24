package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.UserProfileDao
import app.efficientbytes.booleanbear.database.models.LocalNotificationToken
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.UserProfileService
import app.efficientbytes.booleanbear.services.models.NotificationTokenStatus
import app.efficientbytes.booleanbear.services.models.UserProfilePayload
import app.efficientbytes.booleanbear.utils.NoInternetException
import app.efficientbytes.booleanbear.utils.USER_PROFILE_DOCUMENT_PATH
import app.efficientbytes.booleanbear.utils.UserAccountCoroutineScope
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.addSnapshotListenerFlow
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class UserProfileRepository(
    private val userProfileService: UserProfileService,
    private val userProfileDao: UserProfileDao,
    private val externalScope: CoroutineScope,
    private val userProfileCoroutineScope: UserAccountCoroutineScope,
    private val userProfileListener: UserProfileListener
) {

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    private val gson = Gson()

    fun getUserProfile() {
        userProfileListener.postLatestValue(DataStatus.loading())
        externalScope.launch {
            try {
                val response = userProfileService.getUserProfile()
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val responseUserProfile = response.body()
                        responseUserProfile?.let { userProfilePayload ->
                            userProfileListener.postValue(DataStatus.success(userProfilePayload.userProfile))
                        }
                    }

                    responseCode >= 400 -> {
                        val errorResponse: UserProfilePayload = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserProfilePayload::class.java
                        )
                        val message =
                            "Error Code $responseCode. ${errorResponse.message.toString()}"
                        userProfileListener.postValue(DataStatus.failed(message))
                    }
                }
            } catch (noInternet: NoInternetException) {
                userProfileListener.postValue(DataStatus.noInternet())
            } catch (socketTimeOutException: SocketTimeoutException) {
                userProfileListener.postValue(DataStatus.timeOut())
            } catch (exception: IOException) {
                userProfileListener.postValue(DataStatus.unknownException(exception.message.toString()))
            }
        }
    }

    suspend fun updateUserPrivateProfileBasicDetails(userProfile: UserProfile) = flow {
        try {
            emit(DataStatus.loading<UserProfilePayload>())
            val response = userProfileService.updateUserPrivateProfileBasicDetails(
                userProfile.firstName,
                userProfile.phoneNumber,
                userProfile.phoneNumberPrefix,
                userProfile.completePhoneNumber,
                userProfile.userAccountId,
                userProfile.activityId,
                userProfile.profession,
                userProfile.lastName,
                userProfile.emailAddress,
                userProfile.linkedInUsername,
                userProfile.gitHubUsername,
                userProfile.universityName,
                userProfile.createdOn,
                userProfile.lastUpdatedOn
            )
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val responseUserProfile = response.body()
                    emit(DataStatus.success(responseUserProfile))
                }

                responseCode >= 400 -> {
                    val errorResponse: UserProfilePayload = gson.fromJson(
                        response.errorBody()!!.string(),
                        UserProfilePayload::class.java
                    )
                    val message =
                        "Error Code $responseCode. ${errorResponse.message.toString()}"
                    emit(DataStatus.failed<UserProfilePayload>(message))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet<UserProfilePayload>())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut<UserProfilePayload>())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException<UserProfilePayload>(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException<UserProfilePayload>(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun updateUserPrivateProfile(userProfile: UserProfile) = flow {
        try {
            emit(DataStatus.loading())
            val response = userProfileService.updateUserPrivateProfile(
                userProfile.firstName,
                userProfile.phoneNumber,
                userProfile.phoneNumberPrefix,
                userProfile.completePhoneNumber,
                userProfile.userAccountId,
                userProfile.activityId,
                userProfile.profession,
                userProfile.lastName,
                userProfile.emailAddress,
                userProfile.linkedInUsername,
                userProfile.gitHubUsername,
                userProfile.universityName,
                userProfile.createdOn,
                userProfile.lastUpdatedOn
            )
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val responseUserProfile = response.body()
                    if (responseUserProfile != null) emit(
                        DataStatus.success<UserProfilePayload>(
                            responseUserProfile
                        )
                    )
                }

                responseCode >= 400 -> {
                    val errorResponse: UserProfilePayload = gson.fromJson(
                        response.errorBody()!!.string(),
                        UserProfilePayload::class.java
                    )
                    val message =
                        "Error Code $responseCode. ${errorResponse.message.toString()}"
                    emit(DataStatus.failed<UserProfilePayload>(message))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet<UserProfilePayload>())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut<UserProfilePayload>())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException<UserProfilePayload>(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException<UserProfilePayload>(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile)
    }

    suspend fun deleteUserProfile() {
        userProfileDao.delete()
    }

    fun listenToUserProfileChange(userAccountId: String) {
        userProfileCoroutineScope.getScope().launch {
            val userProfileSnapshot =
                Firebase.firestore.collection(USER_PROFILE_DOCUMENT_PATH).document(userAccountId)
            try {
                userProfileSnapshot.addSnapshotListenerFlow().collect {
                    when {
                        it.status == DataStatus.Status.Failed -> {
                            userProfileListener.postLatestValue(it)
                        }

                        it.status == DataStatus.Status.Success -> {
                            userProfileListener.postLatestValue(it)
                        }
                    }
                }
            } catch (noInternet: NoInternetException) {
                userProfileListener.postLatestValue(DataStatus.noInternet())
            } catch (socketTimeOutException: SocketTimeoutException) {
                userProfileListener.postLatestValue(DataStatus.timeOut())
            } catch (exception: IOException) {
                userProfileListener.postLatestValue(DataStatus.unknownException(exception.message.toString()))
            }
        }
    }

    fun uploadNotificationsToken(
        token: String,
        notificationListener: NotificationUploadListener? = null
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            externalScope.launch {
                notificationListener?.onTokenStatusChanged(DataStatus.loading())
                try {
                    val response = userProfileService.uploadNotificationsToken(token)
                    val responseCode = response.code()
                    when {
                        responseCode == 200 -> {
                            val body = response.body()
                            if (body != null) notificationListener?.onTokenStatusChanged(
                                DataStatus.success(
                                    body
                                )
                            ) else notificationListener?.onTokenStatusChanged(DataStatus.emptyResult())
                        }

                        responseCode >= 400 -> {
                            val errorResponse: NotificationTokenStatus = gson.fromJson(
                                response.errorBody()!!.string(),
                                NotificationTokenStatus::class.java
                            )
                            notificationListener?.onTokenStatusChanged(
                                DataStatus.failed(
                                    errorResponse.message.toString()
                                )
                            )
                        }
                    }
                } catch (noInternet: NoInternetException) {
                    notificationListener?.onTokenStatusChanged(
                        DataStatus.noInternet()
                    )
                } catch (socketTimeOutException: SocketTimeoutException) {
                    notificationListener?.onTokenStatusChanged(
                        DataStatus.timeOut()
                    )
                } catch (exception: IOException) {
                    notificationListener?.onTokenStatusChanged(
                        DataStatus.unknownException(exception.message.toString())
                    )
                }
            }
        }
    }

    fun generateFCMToken(notificationListener: NotificationUploadListener) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            externalScope.launch {
                val result = userProfileDao.getFCMToken()
                if (result == null) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }
                        val token = task.result
                        notificationListener.onTokenGenerated(token)
                    })
                }
            }
        }
    }

    fun saveNotificationToken(localNotificationToken: LocalNotificationToken) {
        externalScope.launch {
            userProfileDao.insertFCMToken(localNotificationToken)
        }
    }

    fun deleteLocalNotificationToken() {
        externalScope.launch {
            userProfileDao.deleteFCMToken()
        }
    }

    fun deleteRemoteNotificationToken() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            externalScope.launch {
                userProfileService.deleteFCMToken("")
            }
        }
    }

    interface NotificationUploadListener {

        fun onTokenStatusChanged(status: DataStatus<NotificationTokenStatus>)

        fun onTokenGenerated(token: String)
    }

}