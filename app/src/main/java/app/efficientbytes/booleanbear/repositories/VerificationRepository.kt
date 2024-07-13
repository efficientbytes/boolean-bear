package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.VerificationService
import app.efficientbytes.booleanbear.services.models.LoginModeResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.VerifyPhoneResponse
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

    suspend fun getLoginMode(prefix: String, phoneNumber: String) = flow {
        try {
            emit(DataStatus.loading())
            val response = verificationService.getLoginMode(prefix, phoneNumber)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val loginMode = body.data
                        if (loginMode != null) emit(DataStatus.success(loginMode))
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: LoginModeResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        LoginModeResponse::class.java
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

    suspend fun sendOTPToPhoneNumber(prefix: String, phoneNumber: String) = flow {
        try {
            emit(DataStatus.loading())
            val response = verificationService.sendOtpToPhoneNumber(
                prefix,
                phoneNumber
            )
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val phoneNumberData = body.data
                        if (phoneNumberData != null) {
                            emit(
                                DataStatus.success(
                                    data = phoneNumberData,
                                    message = body.message
                                )
                            )
                        }
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: VerifyPhoneResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        VerifyPhoneResponse::class.java
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

    suspend fun verifyPhoneNumberOTP(prefix: String, phoneNumber: String, otp: String) = flow {
        try {
            emit(DataStatus.loading())
            val response = verificationService.verifyPhoneNumberOTP(
                prefix,
                phoneNumber,
                otp
            )
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val phoneNumberData = body.data
                        if (phoneNumberData != null) {
                            emit(
                                DataStatus.success(
                                    data = phoneNumberData,
                                    message = body.message
                                )
                            )
                        }
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: VerifyPhoneResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        VerifyPhoneResponse::class.java
                    )
                    val message =
                        "Error $responseCode. ${errorResponse.message.toString()}"
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
                        verifyPrimaryEmailAddress.firstName
                    )
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val verificationStatus = response.body()
                        emit(DataStatus.success(verificationStatus))
                    }

                    responseCode in 414..417 -> {
                        emit(DataStatus.unAuthorized(responseCode.toString()))
                    }

                    responseCode >= 400 -> {
                        val errorResponse: ResponseMessage = gson.fromJson(
                            response.errorBody()!!.string(),
                            ResponseMessage::class.java
                        )
                        val message =
                            "Error $responseCode. ${errorResponse.message}"
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