package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.UserProfileService
import app.efficientbytes.androidnow.services.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserProfileRepository(private val userProfileService: UserProfileService) {

    suspend fun getUserProfile(phoneNumber: String? = null, userAccountId: String? = null) = flow {
        emit(DataStatus.loading())
        val response = userProfileService.getUserProfile(
            phoneNumber = phoneNumber,
            userAccountId = userAccountId
        )
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val userProfile = response.body()
                emit(DataStatus.success(userProfile))
            }

            responseCode == 400 -> {
                val userProfile = response.body()
                val errorMessage = "Not yet implemented"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode == 503 -> {
                val userProfile = response.body()
                val errorMessage = "Not yet implemented"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode >= 401 -> emit(DataStatus.failed(response.message()))
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
                val responseUserProfile = response.body()
                val errorMessage = "Not yet implemented"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode == 503 -> {
                val responseUserProfile = response.body()
                val errorMessage = "Not yet implemented"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode >= 401 -> emit(DataStatus.failed(response.message()))
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)


}