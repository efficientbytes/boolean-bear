package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.AuthenticationService
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.SignInToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthenticationRepository(private val authenticationService: AuthenticationService) {

    private val tagAuthenticationRepository = "Authentication Repository"
    private val gson = Gson()
    suspend fun getSignInToken(phoneNumber: PhoneNumber) = flow {
        emit(DataStatus.loading())
        val response = authenticationService.getSignInToken(phoneNumber)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                emit(DataStatus.success(response.body()))
            }

            responseCode >= 400 -> {
                val errorResponse: SignInToken = gson.fromJson(
                    response.errorBody()!!.string(),
                    SignInToken::class.java
                )
                val message = "Error Code $responseCode. ${errorResponse.message.toString()}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

}