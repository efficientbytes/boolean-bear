package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.VerificationService
import app.efficientbytes.booleanbear.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.booleanbear.services.models.PrimaryEmailAddressVerificationStatus
import app.efficientbytes.booleanbear.services.models.VerifyPhoneNumber
import app.efficientbytes.booleanbear.services.models.VerifyPrimaryEmailAddress
import app.efficientbytes.booleanbear.utils.NoInternetException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.SocketTimeoutException

class VerificationRepository(private val verificationService: VerificationService) {

    private val gson = Gson()
    suspend fun sendOTPToPhoneNumber(verifyPhoneNumber: VerifyPhoneNumber) = flow {
        try {
            emit(DataStatus.loading())
            val response = verificationService.sendOtpToPhoneNumber(
                verifyPhoneNumber.phoneNumber,
                verifyPhoneNumber.otp
            )
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun verifyPhoneNumberOTP(verifyPhoneNumber: VerifyPhoneNumber) = flow {
        try {
            emit(DataStatus.loading())
            val response = verificationService.verifyPhoneNumberOTP(
                verifyPhoneNumber.phoneNumber,
                verifyPhoneNumber.otp
            )
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun verifyPrimaryEmailAddress(verifyPrimaryEmailAddress: VerifyPrimaryEmailAddress) =
        flow {
            try {
                emit(DataStatus.loading())
                val response =
                    verificationService.verifyPrimaryEmailAddress(
                        verifyPrimaryEmailAddress.emailAddress,
                        verifyPrimaryEmailAddress.userAccountId,
                        verifyPrimaryEmailAddress.firstName
                    )
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
                        val message =
                            "Error Code $responseCode. ${errorResponse.message.toString()}"
                        emit(DataStatus.failed(message))
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