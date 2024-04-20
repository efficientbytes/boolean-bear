package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.AuthenticationDao
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.repositories.models.AuthState
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AuthenticationService
import app.efficientbytes.booleanbear.services.models.DeleteUserAccount
import app.efficientbytes.booleanbear.services.models.DeleteUserAccountStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import app.efficientbytes.booleanbear.services.models.SignInToken
import app.efficientbytes.booleanbear.utils.AuthStateCoroutineScope
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.IDTokenListener
import app.efficientbytes.booleanbear.utils.NoInternetException
import app.efficientbytes.booleanbear.utils.SINGLE_DEVICE_LOGIN_DOCUMENT_PATH
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.addSnapshotListenerFlow
import app.efficientbytes.booleanbear.utils.authStateFlow
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
import java.io.IOException
import java.net.SocketTimeoutException

class AuthenticationRepository(
    private val authenticationService: AuthenticationService,
    private val authenticationDao: AuthenticationDao,
    private val externalScope: CoroutineScope,
    private val authStateCoroutineScope: AuthStateCoroutineScope,
    private val singleDeviceLoginListener: SingleDeviceLoginListener,
    private val customAuthStateListener: CustomAuthStateListener
) {

    private val gson = Gson()
    suspend fun getSignInToken(phoneNumber: PhoneNumber) = flow {
        try {
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun getSingleDeviceLogin(userAccountId: String) = flow {
        try {
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
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
            try {
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
            } catch (noInternet: NoInternetException) {
                singleDeviceLoginListener.postValue(DataStatus.noInternet())
            } catch (socketTimeOutException: SocketTimeoutException) {
                singleDeviceLoginListener.postValue(DataStatus.timeOut())
            } catch (exception: IOException) {
                singleDeviceLoginListener.postValue(DataStatus.unknownException(exception.message.toString()))
            }
        }
    }

    suspend fun deleteUserAccount(deleteUserAccount: DeleteUserAccount) = flow {
        try {
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
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { emit(DataStatus.unknownException(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    fun listenForAuthStateChanges() {
        authStateCoroutineScope.getScope().launch {
            try {
                val auth = FirebaseAuth.getInstance()
                auth.authStateFlow().collect { authState ->
                    customAuthStateListener.postValue(authState is AuthState.Authenticated)
                }
            } catch (exception: Exception) {
            }
        }
    }

    fun generateIDToken(idTokenListener: IDTokenListener) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            externalScope.launch {
                currentUser.getIdToken(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idToken: String? = task.result.token
                            idTokenListener.onIDTokenGenerated(idToken)
                        } else {
                            idTokenListener.onIDTokenGenerated()
                        }
                    }
            }
        }
    }

    fun deleteIDToken() {
        externalScope.launch {
            authenticationDao.deleteIDTokenTable()
        }
    }

    fun saveIDToken(idToken: IDToken) {
        externalScope.launch {
            authenticationDao.insertIDToken(idToken)
        }
    }

}