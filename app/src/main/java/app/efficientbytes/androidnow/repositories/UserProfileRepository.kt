package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.database.dao.UserProfileDao
import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.UserProfileService
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

    suspend fun getUserProfile(phoneNumber: String? = null, userAccountId: String? = null) = flow {
        emit(DataStatus.loading())
        val response = userProfileService.getUserProfile(
            phoneNumber = phoneNumber,
            userAccountId = userAccountId
        )
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val responseUserProfile = response.body()
                emit(DataStatus.success(responseUserProfile))
            }

            responseCode == 400 -> {
                val errorMessage = response.body()?.message
                emit(DataStatus.failed(errorMessage ?: "Server message could not be read."))
            }

            responseCode == 503 -> {
                val errorMessage = response.body()?.message
                emit(DataStatus.failed(errorMessage ?: "Server message could not be read."))
            }

            responseCode >= 401 -> {
                val errorMessage = response.body()?.message
                emit(DataStatus.failed(errorMessage ?: "Server message could not be read."))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun updateUserProfile(userProfile: UserProfile) = flow {
        emit(DataStatus.loading())
        val response = userProfileService.updateUserProfile(userProfile)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val responseUserProfile = response.body()
                emit(DataStatus.success(responseUserProfile))
            }

            responseCode == 400 -> {
                val errorMessage = response.body()?.message
                emit(DataStatus.failed(errorMessage ?: "Server message could not be read."))
            }

            responseCode == 503 -> {
                val errorMessage = response.body()?.message
                emit(DataStatus.failed(errorMessage ?: "Server message could not be read."))
            }

            responseCode >= 401 -> {
                val errorMessage = response.body()?.message
                emit(DataStatus.failed(errorMessage ?: "Server message could not be read."))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfile.apply {
            rowId = 1
        }
        userProfileDao.insertUserProfile(userProfile)
    }


}