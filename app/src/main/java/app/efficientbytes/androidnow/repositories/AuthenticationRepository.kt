package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.database.dao.AuthenticationDao
import app.efficientbytes.androidnow.models.SingleDeviceLogin
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.AuthenticationService
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.SignInToken
import app.efficientbytes.androidnow.utils.SINGLE_DEVICE_LOGIN_DOCUMENT_PATH
import app.efficientbytes.androidnow.utils.addSnapshotListenerFlow
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthenticationRepository(
    private val authenticationService: AuthenticationService,
    private val authenticationDao: AuthenticationDao
) {

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

    suspend fun getSingleDeviceLogin(userAccountId: String) = flow {
        emit(DataStatus.loading())
        val response = authenticationService.getSingleDeviceLogin(userAccountId)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                emit(DataStatus.success(response.body()))
            }

            responseCode >= 400 -> {
                val errorResponse: SingleDeviceLogin = gson.fromJson(
                    response.errorBody()!!.string(),
                    SingleDeviceLogin::class.java
                )
                val message = "Error Code $responseCode. ${errorResponse.message}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    val singleDeviceLoginFromDB: Flow<SingleDeviceLogin> = authenticationDao.getSingleDeviceLogin()

    suspend fun saveSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin) {
        authenticationDao.insertSingleDeviceLogin(singleDeviceLogin)
    }

    suspend fun deleteSingleDeviceLogin() {
        authenticationDao.delete()
    }

    suspend fun listenToSingleDeviceLoginChange(userAccountId: String) = flow {
        val singleDeviceLoginSnapshot =
            Firebase.firestore.collection(SINGLE_DEVICE_LOGIN_DOCUMENT_PATH).document(userAccountId)
        singleDeviceLoginSnapshot.addSnapshotListenerFlow().collect {
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