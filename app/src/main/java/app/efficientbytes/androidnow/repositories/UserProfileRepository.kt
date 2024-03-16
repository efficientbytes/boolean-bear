package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.database.dao.UserProfileDao
import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.UserProfileService
import app.efficientbytes.androidnow.services.models.UserProfilePayload
import app.efficientbytes.androidnow.utils.USER_PROFILE_DOCUMENT_PATH
import app.efficientbytes.androidnow.utils.addSnapshotListenerFlow
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserProfileRepository(
    private val userProfileService: UserProfileService,
    private val userProfileDao: UserProfileDao
) {

    private val tagUserProfileRepository = "User-Profile-Repository"
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    private val gson = Gson()

    suspend fun getUserProfile(userAccountId: String? = null) = flow {
        emit(DataStatus.loading())
        val response = userProfileService.getUserProfile(
            userAccountId = userAccountId
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
                val message = "Error Code $responseCode. ${errorResponse.message.toString()}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

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

    suspend fun listenToUserProfileChange(userAccountId: String) = flow {
        val userProfileSnapshot =
            Firebase.firestore.collection(USER_PROFILE_DOCUMENT_PATH).document(userAccountId)
        userProfileSnapshot.addSnapshotListenerFlow().collect {
            when {
                it.status == DataStatus.Status.Failed -> {
                    emit(it)
                }

                it.status == DataStatus.Status.Success -> {
                    emit(it)
                }
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)


}