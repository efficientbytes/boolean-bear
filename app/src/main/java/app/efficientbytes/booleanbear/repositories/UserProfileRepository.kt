package app.efficientbytes.booleanbear.repositories

import android.util.Log
import app.efficientbytes.booleanbear.database.dao.UserProfileDao
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.UserProfileService
import app.efficientbytes.booleanbear.services.models.UserProfilePayload
import app.efficientbytes.booleanbear.utils.USER_PROFILE_DOCUMENT_PATH
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.addSnapshotListenerFlow
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Response

class UserProfileRepository(
    private val userProfileService: UserProfileService,
    private val userProfileDao: UserProfileDao,
    private val externalScope: CoroutineScope,
    private val userProfileListener: UserProfileListener
) {

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    private val gson = Gson()

    fun getUserProfile(userAccountId: String) {
        userProfileListener.postLatestValue(DataStatus.loading())
        externalScope.launch {
            val response: Response<UserProfilePayload>
            try {
                response = userProfileService.getUserProfile(userAccountId = userAccountId)
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
            } catch (exception: Exception) {
                userProfileListener.postValue(DataStatus.failed(exception.message.toString()))
            }
        }
    }

    suspend fun updateUserPrivateProfileBasicDetails(userProfile: UserProfile) = flow {
        emit(DataStatus.loading())
        val response = userProfileService.updateUserPrivateProfileBasicDetails(userProfile)
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
                val message = "Error Code $responseCode. ${errorResponse.message.toString()}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun updateUserPrivateProfile(userProfile: UserProfile) = flow {
        emit(DataStatus.loading())
        val response = userProfileService.updateUserPrivateProfile(userProfile)
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
                val message = "Error Code $responseCode. ${errorResponse.message.toString()}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile)
    }

    suspend fun deleteUserProfile() {
        userProfileDao.delete()
    }

    fun listenToUserProfileChange(userAccountId: String) {
        externalScope.launch {
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
            } catch (exception: Exception) {
                userProfileListener.postLatestValue(DataStatus.failed(exception.message.toString()))
            }
        }
    }


}