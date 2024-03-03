package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.AuthenticationService
import app.efficientbytes.androidnow.services.models.PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthenticationRepository(private val authenticationService: AuthenticationService) {

    private val tagVerificationRepository = "Authentication Repository"
    suspend fun getSignInToken(phoneNumber: PhoneNumber) = flow {
        emit(DataStatus.loading())
        val response = authenticationService.getSignInToken(phoneNumber)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                emit(DataStatus.success(response.body()))
            }

            responseCode >= 400 -> emit(DataStatus.failed("OTP processing failed : Error code $responseCode"))
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

}