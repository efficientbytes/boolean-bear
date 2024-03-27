package app.efficientbytes.androidnow.repositories

import android.util.Log
import app.efficientbytes.androidnow.database.dao.AuthenticationDao
import app.efficientbytes.androidnow.models.SingleDeviceLogin
import app.efficientbytes.androidnow.repositories.models.AuthState
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.AuthenticationService
import app.efficientbytes.androidnow.services.models.DeleteUserAccount
import app.efficientbytes.androidnow.services.models.DeleteUserAccountStatus
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.SignInToken
import app.efficientbytes.androidnow.utils.AuthStateCoroutineScope
import app.efficientbytes.androidnow.utils.CustomAuthStateListener
import app.efficientbytes.androidnow.utils.SINGLE_DEVICE_LOGIN_DOCUMENT_PATH
import app.efficientbytes.androidnow.utils.SingleDeviceLoginListener
import app.efficientbytes.androidnow.utils.addSnapshotListenerFlow
import app.efficientbytes.androidnow.utils.authStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AuthenticationRepository(
    private val authenticationService: AuthenticationService,
    private val authenticationDao: AuthenticationDao,
    private val externalScope: CoroutineScope,
    private val authStateCoroutineScope: AuthStateCoroutineScope,
    private val singleDeviceLoginListener: SingleDeviceLoginListener,
    private val customAuthStateListener: CustomAuthStateListener
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

    fun listenToSingleDeviceLoginChange(userAccountId: String) {
        externalScope.launch {
            Log.i(tagAuthenticationRepository, "Inside the external scope of auth rep")
            val singleDeviceLoginSnapshot =
                Firebase.firestore.collection(SINGLE_DEVICE_LOGIN_DOCUMENT_PATH)
                    .document(userAccountId)
            singleDeviceLoginSnapshot.addSnapshotListenerFlow().collect {
                when {
                    it.status == DataStatus.Status.Failed -> {
                        singleDeviceLoginListener.postValue(it)
                    }

                    it.status == DataStatus.Status.Success -> {
                        singleDeviceLoginListener.postValue(it)
                    }
                }
            }
        }
    }

    suspend fun deleteUserAccount(deleteUserAccount: DeleteUserAccount) = flow {
        emit(DataStatus.loading())
        val response = authenticationService.deleteUserAccount(deleteUserAccount)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                emit(DataStatus.success(response.body()))
            }

            responseCode >= 400 -> {
                val errorResponse: DeleteUserAccountStatus = gson.fromJson(
                    response.errorBody()!!.string(),
                    DeleteUserAccountStatus::class.java
                )
                val message = "Error Code $responseCode. ${errorResponse.message}"
                emit(DataStatus.failed(message))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    fun listenForAuthStateChanges() {
        authStateCoroutineScope.getScope().launch {
            val auth = FirebaseAuth.getInstance()
            auth.authStateFlow().collect { authState ->
                Log.i(tagAuthenticationRepository, "Auth State is : $authState")
                customAuthStateListener.postValue(authState is AuthState.Authenticated)
            }
        }
    }


}