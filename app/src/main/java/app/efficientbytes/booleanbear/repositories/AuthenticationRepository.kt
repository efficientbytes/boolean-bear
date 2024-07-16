package app.efficientbytes.booleanbear.repositories

import androidx.lifecycle.LiveData
import app.efficientbytes.booleanbear.database.dao.AuthenticationDao
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.models.LocalBooleanFlag
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.SingleDeviceLoginResponse
import app.efficientbytes.booleanbear.repositories.models.AuthState
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AuthenticationService
import app.efficientbytes.booleanbear.services.models.PasswordAuthenticationResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.SignInTokenResponse
import app.efficientbytes.booleanbear.utils.AppAuthStateListener
import app.efficientbytes.booleanbear.utils.AuthStateCoroutineScope
import app.efficientbytes.booleanbear.utils.IDTokenListener
import app.efficientbytes.booleanbear.utils.NoInternetException
import app.efficientbytes.booleanbear.utils.PASSWORD_CREATED_FLAG
import app.efficientbytes.booleanbear.utils.SINGLE_DEVICE_LOGIN_DOCUMENT_PATH
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginCoroutineScope
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.addSnapshotListenerFlow
import app.efficientbytes.booleanbear.utils.authStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class AuthenticationRepository(
    private val authenticationService: AuthenticationService,
    private val authenticationDao: AuthenticationDao,
    private val authStateCoroutineScope: AuthStateCoroutineScope,
    private val singleDeviceLoginCoroutineScope: SingleDeviceLoginCoroutineScope,
    private val singleDeviceLoginListener: SingleDeviceLoginListener,
    private val appAuthStateListener: AppAuthStateListener
) {

    private val gson = Gson()

    suspend fun getSignInToken(prefix: String, phoneNumber: String) = flow {
        try {
            emit(DataStatus.loading())
            val response = authenticationService.getSignInToken(prefix, phoneNumber)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val signInTokenResponse = response.body()
                    val signInToken = signInTokenResponse?.data
                    if (signInToken != null) {
                        emit(DataStatus.success(signInToken))
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: SignInTokenResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        SignInTokenResponse::class.java
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

    val liveSingleDeviceLoginFromLocal: LiveData<SingleDeviceLogin?> =
        authenticationDao.getLiveSingleDeviceLogin()

    fun getSingleDeviceLoginFromRemote() {
        singleDeviceLoginCoroutineScope.getScope().launch {
            try {
                singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(DataStatus.loading())
                val response = authenticationService.getSingleDeviceLogin()
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val body = response.body()
                        if (body != null) {
                            val singleDeviceLogin = body.data
                            if (singleDeviceLogin != null) {
                                singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(
                                    DataStatus.success(
                                        singleDeviceLogin
                                    )
                                )
                            }
                        }
                    }

                    responseCode in 414..417 -> {
                        singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(
                            DataStatus.unAuthorized(responseCode.toString())
                        )
                    }

                    responseCode >= 400 -> {
                        val errorResponse: SingleDeviceLoginResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            SingleDeviceLoginResponse::class.java
                        )
                        val message = "Error Code $responseCode. ${errorResponse.message}"
                        singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(
                            DataStatus.failed(
                                message
                            )
                        )
                    }
                }
            } catch (noInternet: NoInternetException) {
                singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(DataStatus.noInternet())
            } catch (socketTimeOutException: SocketTimeoutException) {
                singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(DataStatus.timeOut())
            } catch (exception: IOException) {
                singleDeviceLoginListener.updateSingleDeviceLoginFromRemote(
                    DataStatus.unknownException(
                        exception.message.toString()
                    )
                )
            }
        }
    }

    fun getSingleDeviceLoginFromLocal() = authenticationDao.getSingleDeviceLogin()

    fun saveSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin) {
        singleDeviceLoginCoroutineScope.getScope().launch {
            authenticationDao.insertSingleDeviceLogin(singleDeviceLogin)
        }
    }

    fun getLiveSingleDeviceLoginFromRemote(userAccountId: String) {
        if (singleDeviceLoginCoroutineScope.scopeStatus() == null) {
            singleDeviceLoginCoroutineScope.getScope().launch {
                try {
                    val singleDeviceLoginSnapshot =
                        Firebase.firestore.collection(SINGLE_DEVICE_LOGIN_DOCUMENT_PATH)
                            .document(userAccountId)

                    singleDeviceLoginSnapshot.addSnapshotListenerFlow().collect {
                        when {
                            it.status == DataStatus.Status.Failed -> {
                                singleDeviceLoginListener.updateLiveSingleDeviceLoginFromRemote(it)
                            }

                            it.status == DataStatus.Status.Success -> {
                                singleDeviceLoginListener.updateLiveSingleDeviceLoginFromRemote(it)
                            }
                        }
                    }
                } catch (noInternet: NoInternetException) {
                    singleDeviceLoginListener.updateLiveSingleDeviceLoginFromRemote(DataStatus.noInternet())
                } catch (socketTimeOutException: SocketTimeoutException) {
                    singleDeviceLoginListener.updateLiveSingleDeviceLoginFromRemote(DataStatus.timeOut())
                } catch (exception: IOException) {
                    singleDeviceLoginListener.updateLiveSingleDeviceLoginFromRemote(
                        DataStatus.unknownException(
                            exception.message.toString()
                        )
                    )
                }
            }
        }
    }

    fun resetSingleDeviceScope() {
        if (singleDeviceLoginCoroutineScope.scopeStatus() != null) {
            singleDeviceLoginCoroutineScope.resetScope()
        }
    }

    fun resetSingleDeviceLoginListener() {
        singleDeviceLoginListener.resetAll()
    }

    suspend fun resetSingleDeviceLoginInLocal() {
        authenticationDao.deleteSingleDeviceLogin()
    }

    suspend fun deleteUserAccount() = flow {
        try {
            emit(DataStatus.loading())
            val response = authenticationService.deleteUserAccount()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    emit(DataStatus.success(response.body()))
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: ResponseMessage = gson.fromJson(
                        response.errorBody()!!.string(),
                        ResponseMessage::class.java
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

    fun getLiveAuthStateFromRemote() {
        if (authStateCoroutineScope.scopeStatus() == null) {
            authStateCoroutineScope.getScope().launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    auth.authStateFlow().collect { authState ->
                        appAuthStateListener.updateLiveAuthStateFromRemote(authState is AuthState.Authenticated)
                    }
                } catch (exception: Exception) {
                }
            }
        }
    }

    fun resetAuthScope() {
        if (authStateCoroutineScope.scopeStatus() != null) {
            authStateCoroutineScope.resetScope()
        }
    }

    fun generateIDToken(idTokenListener: IDTokenListener) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken: String? = task.result.token
                idTokenListener.onIDTokenGenerated(idToken)
            } else {
                idTokenListener.onIDTokenGenerated()
            }
        }
    }

    suspend fun deleteIDToken() {
        authenticationDao.deleteIDTokenTable()
    }

    fun saveIDToken(idToken: IDToken) {
        authStateCoroutineScope.getScope().launch {
            authenticationDao.insertIDToken(idToken)
        }
    }

    fun insertPasswordCreated(value: Boolean) {
        authStateCoroutineScope.getScope().launch {
            authenticationDao.insertPasswordCreatedFlag(
                LocalBooleanFlag(
                    PASSWORD_CREATED_FLAG,
                    value
                )
            )
        }
    }

    fun createAccountPassword(password: String) = flow {
        emit(DataStatus.loading())
        try {
            val response = authenticationService.createAccountPassword(password)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    emit(DataStatus.success(true, message = response.body()?.message))
                    authenticationDao.insertPasswordCreatedFlag(
                        LocalBooleanFlag(
                            PASSWORD_CREATED_FLAG, true
                        )
                    )
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: ResponseMessage = gson.fromJson(
                        response.errorBody()!!.string(),
                        ResponseMessage::class.java
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

    fun updateAccountPassword(password: String) = flow {
        emit(DataStatus.loading())
        try {
            val response = authenticationService.updateAccountPassword(password)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    emit(DataStatus.success(data = true, message = response.body()?.message))
                    authenticationDao.insertPasswordCreatedFlag(
                        LocalBooleanFlag(
                            PASSWORD_CREATED_FLAG, true
                        )
                    )
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: ResponseMessage = gson.fromJson(
                        response.errorBody()!!.string(),
                        ResponseMessage::class.java
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

    fun isUserPasswordCreated() = flow {
        val result = authenticationDao.getPasswordCreated(PASSWORD_CREATED_FLAG)
        emit(result)
    }.catch { emit(null) }
        .flowOn(Dispatchers.IO)

    suspend fun deletePasswordCreated() {
        authenticationDao.deletePasswordCreatedFlag(PASSWORD_CREATED_FLAG)
    }

    fun authenticateWithPassword(userAccountId: String, password: String) = flow {
        emit(DataStatus.loading())
        try {
            val response = authenticationService.authenticateWithPassword(userAccountId, password)
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val phoneNumber = body.data
                        if (phoneNumber != null) {
                            emit(DataStatus.success(phoneNumber))
                        }
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    val errorResponse: PasswordAuthenticationResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        PasswordAuthenticationResponse::class.java
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

}