package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.VerificationService
import app.efficientbytes.androidnow.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.androidnow.services.models.PrimaryEmailAddressVerificationStatus
import app.efficientbytes.androidnow.services.models.VerifyPhoneNumber
import app.efficientbytes.androidnow.services.models.VerifyPrimaryEmailAddress
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class VerificationRepository(private val verificationService: VerificationService) {

    private val tagVerificationRepository = "Verification Repository"
    private val gson = Gson()
    suspend fun sendOTPToPhoneNumber(verifyPhoneNumber: VerifyPhoneNumber) = flow {
        emit(DataStatus.loading())
        val response = verificationService.sendOtpToPhoneNumber(verifyPhoneNumber)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val phoneNumberVerificationStatus = response.body()
                emit(DataStatus.success(phoneNumberVerificationStatus))
            }

            responseCode >= 400 -> {
                val errorResponse: PhoneNumberVerificationStatus = gson.fromJson(
                    response.errorBody()!!.string(),
                    PhoneNumberVerificationStatus::class.java
                )
                emit(DataStatus.failed(errorResponse.message.toString()))
            }
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

            responseCode >= 400 -> {
                val errorResponse: PhoneNumberVerificationStatus = gson.fromJson(
                    response.errorBody()!!.string(),
                    PhoneNumberVerificationStatus::class.java
                )
                val message = "Error Code $responseCode. ${errorResponse.message.toString()}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun verifyPrimaryEmailAddress(verifyPrimaryEmailAddress: VerifyPrimaryEmailAddress) =
        flow {
            emit(DataStatus.loading())
            val response = verificationService.verifyPrimaryEmailAddress(verifyPrimaryEmailAddress)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val verificationStatus = response.body()
                    emit(DataStatus.success(verificationStatus))
                }

                responseCode >= 400 -> {
                    val errorResponse: PrimaryEmailAddressVerificationStatus = gson.fromJson(
                        response.errorBody()!!.string(),
                        PrimaryEmailAddressVerificationStatus::class.java
                    )
                    val message = "Error Code $responseCode. ${errorResponse.message.toString()}"
                    emit(DataStatus.failed(message))
                }
            }
        }


}