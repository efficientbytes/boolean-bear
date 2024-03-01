package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.VerificationService
import app.efficientbytes.androidnow.services.models.VerifyPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class VerificationRepository(private val verificationService: VerificationService) {

    suspend fun sendOTPToPhoneNumber(verifyPhoneNumber: VerifyPhoneNumber) = flow {
        emit(DataStatus.loading())
        val response = verificationService.sendOtpToPhoneNumber(verifyPhoneNumber)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val phoneNumberVerificationStatus = response.body()
                emit(DataStatus.success(phoneNumberVerificationStatus))
            }

            responseCode == 400 -> {
                val phoneNumberVerificationStatus = response.body()
                val errorMessage =
                    "${phoneNumberVerificationStatus?.status} : ${phoneNumberVerificationStatus?.message}"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode == 503 -> {
                val phoneNumberVerificationStatus = response.body()
                val errorMessage =
                    "${phoneNumberVerificationStatus?.status} : ${phoneNumberVerificationStatus?.message}"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode >= 401 -> emit(DataStatus.failed(response.message()))
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun verifyPhoneNumberOTP(verifyPhoneNumber: VerifyPhoneNumber) = flow {
        emit(DataStatus.loading())
        val response = verificationService.verifyPhoneNumberOTP(verifyPhoneNumber)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val phoneNumberVerificationStatus = response.body()
                emit(DataStatus.success(phoneNumberVerificationStatus))
            }

            responseCode == 400 || responseCode == 401 || responseCode == 503 -> {
                val phoneNumberVerificationStatus = response.body()
                val errorMessage =
                    "${phoneNumberVerificationStatus?.status} : ${phoneNumberVerificationStatus?.message}"
                emit(DataStatus.failed(errorMessage))
            }

            responseCode >= 402 -> emit(DataStatus.failed(response.message()))
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)


}