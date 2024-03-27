package app.efficientbytes.androidnow.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.models.SingleDeviceLogin
import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.repositories.AuthenticationRepository
import app.efficientbytes.androidnow.repositories.FeedbackNSupportRepository
import app.efficientbytes.androidnow.repositories.UserProfileRepository
import app.efficientbytes.androidnow.repositories.UtilityDataRepository
import app.efficientbytes.androidnow.repositories.VerificationRepository
import app.efficientbytes.androidnow.repositories.models.AuthState
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.DeleteUserAccount
import app.efficientbytes.androidnow.services.models.DeleteUserAccountStatus
import app.efficientbytes.androidnow.services.models.IssueCategory
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.androidnow.services.models.Profession
import app.efficientbytes.androidnow.services.models.RequestSupport
import app.efficientbytes.androidnow.services.models.RequestSupportStatus
import app.efficientbytes.androidnow.services.models.SignInToken
import app.efficientbytes.androidnow.services.models.UserProfilePayload
import app.efficientbytes.androidnow.services.models.VerifyPhoneNumber
import app.efficientbytes.androidnow.utils.authStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import java.net.InetAddress

class MainViewModel(
    private val application: Application,
    private val authenticationRepository: AuthenticationRepository,
    private val userProfileRepository: UserProfileRepository,
    private val utilityDataRepository: UtilityDataRepository,
    private val verificationRepository: VerificationRepository,
    private val feedbackNSupportRepository: FeedbackNSupportRepository
) : AndroidViewModel(application),
    LifecycleEventObserver {

    private val tagMainViewModel: String = "Main View Model"
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val _signInToken: MutableLiveData<DataStatus<SignInToken?>> = MutableLiveData()
    val signInToken: LiveData<DataStatus<SignInToken?>> = _signInToken
    fun getSignInToken(phoneNumber: PhoneNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getSignInToken(phoneNumber).collect {
                _signInToken.postValue(it)
            }
        }
    }

    private val _isUserSignedIn: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val isUserSignedIn: LiveData<DataStatus<Boolean>> = _isUserSignedIn
    fun signInWithToken(token: SignInToken) {
        viewModelScope.launch(Dispatchers.IO) {
            token.token?.let {
                auth.signInWithCustomToken(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isUserSignedIn.postValue(DataStatus.success(true))
                        } else _isUserSignedIn.postValue(DataStatus.failed(task.exception?.message.toString()))
                    }.addOnFailureListener { exception ->
                        _isUserSignedIn.postValue(DataStatus.failed(exception.message.toString()))
                    }
            }
        }
    }

    private val _firebaseUserToken: MutableLiveData<DataStatus<GetTokenResult>> = MutableLiveData()
    val firebaseUserToken: LiveData<DataStatus<GetTokenResult>> = _firebaseUserToken

    fun getFirebaseUserToken(refresh: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = auth.currentUser
            currentUser?.let {
                it.getIdToken(true)
                    .addOnSuccessListener { result ->
                        Log.i(tagMainViewModel, "User claims is  ${result.claims}")
                        _firebaseUserToken.postValue(DataStatus.success(result))
                    }
            }
        }
    }

    private var authStateListenerJob: Job? = null
    private val _authState: MutableLiveData<Boolean> = MutableLiveData()
    val authState: LiveData<Boolean> = _authState

    fun listenForAuthStateChanges() {
        Log.i(tagMainViewModel, "Auth state listener invoked")
        Log.i(
            tagMainViewModel,
            "Auth state listener coroutine status is active? ${authStateListenerJob?.isActive}"
        )
        authStateListenerJob = viewModelScope.launch {
            auth.authStateFlow().collect { authState ->
                Log.i(tagMainViewModel, "Auth State is : $authState")
                _authState.postValue(authState is AuthState.Authenticated)
            }
        }
        Log.i(
            tagMainViewModel,
            "Auth state listener coroutine status is active? ${authStateListenerJob?.isActive}"
        )
    }

    fun cancelListeningToAuthState() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(tagMainViewModel, "Cancel listening to auth state changes function invoked")
            if (authStateListenerJob != null) {
                if (authStateListenerJob?.isActive == true) {
                    Log.i(
                        tagMainViewModel,
                        "Auth state listener coroutine status is active before cancelling ? ${authStateListenerJob?.isActive}"
                    )
                    authStateListenerJob?.cancelAndJoin()
                    Log.i(
                        tagMainViewModel,
                        "Auth state listener coroutine status is active after cancelling ? ${authStateListenerJob?.isActive}"
                    )
                    authStateListenerJob = null
                } else {
                    Log.i(
                        tagMainViewModel,
                        "Auth state listener coroutine status is not active"
                    )
                }
            }
        }
    }

    val listenToUserProfileFromDB = userProfileRepository.userProfile.asLiveData()
    private val _userProfile: MutableLiveData<DataStatus<UserProfilePayload?>> = MutableLiveData()
    val userProfile: LiveData<DataStatus<UserProfilePayload?>> = _userProfile

    fun getUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            auth.currentUser?.let { firebaseUser ->
                userProfileRepository.getUserProfile(firebaseUser.uid).collect {
                    _userProfile.postValue(it)
                }
            }
        }
    }

    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.saveUserProfile(userProfile)
        }
    }

    fun deleteUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.deleteUserProfile()
        }
    }

    private var userProfileListenerJob: Job? = null
    private val _userProfileLiveDocument: MutableLiveData<DataStatus<DocumentSnapshot?>> =
        MutableLiveData()
    val userProfileLiveDocument: LiveData<DataStatus<DocumentSnapshot?>> = _userProfileLiveDocument

    fun listenToUserProfileChanges(userAccountId: String) {
        Log.i(tagMainViewModel, "Listening to user profile")
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.i(
                tagMainViewModel,
                "User profile listener coroutine job is active? ${userProfileListenerJob?.isActive}"
            )
            userProfileListenerJob = viewModelScope.launch {
                userProfileRepository.listenToUserProfileChange(userAccountId).collect {
                    _userProfileLiveDocument.postValue(it)
                }
            }
            Log.i(
                tagMainViewModel,
                "User profile listener coroutine job is active? ${userProfileListenerJob?.isActive}"
            )
        }
    }

    fun cancelListeningToUserProfileChanges() = viewModelScope.launch {
        Log.i(tagMainViewModel, "Cancel listening to user profile changes")
        if (userProfileListenerJob != null) {
            if (userProfileListenerJob?.isActive == true) {
                Log.i(
                    tagMainViewModel,
                    "User profile listener coroutine status job is active? ${userProfileListenerJob?.isActive}"
                )
                userProfileListenerJob?.cancelAndJoin()
                Log.i(
                    tagMainViewModel,
                    "User profile listener coroutine status job is active? ${userProfileListenerJob?.isActive}"
                )
                userProfileListenerJob = null
            } else {
                Log.i(
                    tagMainViewModel,
                    "User profile listener coroutine status is active? ${userProfileListenerJob?.isActive}"
                )
            }
        } else {
            Log.i(
                tagMainViewModel,
                "User profile listener coroutine status is active? ${userProfileListenerJob?.isActive}"
            )
        }
    }

    val singleDeviceLoginFromDB: LiveData<SingleDeviceLogin?> =
        authenticationRepository.singleDeviceLoginFromDB.asLiveData()
    private val _singleDeviceLoginFromServer: MutableLiveData<DataStatus<SingleDeviceLogin?>> =
        MutableLiveData()
    val singleDeviceLoginFromServer: LiveData<DataStatus<SingleDeviceLogin?>> =
        _singleDeviceLoginFromServer

    fun getSingleDeviceLogin(
        userAccountId: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getSingleDeviceLogin(userAccountId).collect {
                _singleDeviceLoginFromServer.postValue(it)
            }
        }
    }

    fun saveSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.saveSingleDeviceLogin(singleDeviceLogin)
        }
    }

    fun deleteSingleDeviceLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteSingleDeviceLogin()
        }
    }

    private var singleDeviceLoginListenerJob: Job? = null
    private val _singleDeviceLoginLiveDocument: MutableLiveData<DataStatus<DocumentSnapshot?>> =
        MutableLiveData()
    val singleDeviceLiveDocument: LiveData<DataStatus<DocumentSnapshot?>> =
        _singleDeviceLoginLiveDocument

    fun listenToSingleDeviceLoginChanges(userAccountId: String) {
        Log.i(tagMainViewModel, "Listening to single device login")
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.i(
                tagMainViewModel,
                "Single device login listener coroutine job is active? ${userProfileListenerJob?.isActive}"
            )
            singleDeviceLoginListenerJob = viewModelScope.launch {
                authenticationRepository.listenToSingleDeviceLoginChange(userAccountId).collect {
                    _singleDeviceLoginLiveDocument.postValue(it)
                }
            }
            Log.i(
                tagMainViewModel,
                "Single device login listener coroutine job is active? ${userProfileListenerJob?.isActive}"
            )
        }
    }

    fun cancelListeningToSingleDeviceLoginChanges() = viewModelScope.launch {
        Log.i(tagMainViewModel, "Cancel listening to single device login changes")
        if (singleDeviceLoginListenerJob != null) {
            if (singleDeviceLoginListenerJob?.isActive == true) {
                Log.i(
                    tagMainViewModel,
                    "Single device login listener coroutine status job is active? ${singleDeviceLoginListenerJob?.isActive}"
                )
                singleDeviceLoginListenerJob?.cancelAndJoin()
                Log.i(
                    tagMainViewModel,
                    "Single device login listener coroutine status job is active? ${singleDeviceLoginListenerJob?.isActive}"
                )
                singleDeviceLoginListenerJob = null
            } else {
                Log.i(
                    tagMainViewModel,
                    "Single device login listener coroutine status is active? ${singleDeviceLoginListenerJob?.isActive}"
                )
            }
        } else {
            Log.i(
                tagMainViewModel,
                "Single device login listener coroutine status is active? ${singleDeviceLoginListenerJob?.isActive}"
            )
        }
    }

    fun signOutUser() {
        if (auth.currentUser != null) {
            auth.signOut()
        }
    }

    private val _serverTime =
        MutableLiveData<DataStatus<Long?>>()
    val serverTime: LiveData<DataStatus<Long?>> = _serverTime
    fun fetchServerTime() {
        _serverTime.postValue(DataStatus.loading())
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                val timeServer = "time.google.com"
                val client = NTPUDPClient()
                client.defaultTimeout = 10_000
                try {
                    val inetAddress = InetAddress.getByName(timeServer)
                    val timeInfo: TimeInfo = client.getTime(inetAddress)
                    val time = timeInfo.message.receiveTimeStamp.time
                    _serverTime.postValue(DataStatus.success(time))
                } catch (e: Exception) {
                    _serverTime.postValue(DataStatus.failed(e.message.toString()))
                } finally {
                    client.close()
                }
            }
        }
    }

    val professionAdapterList: LiveData<MutableList<Profession>> =
        utilityDataRepository.professionAdapterListFromDB.asLiveData()

    private fun getProfessionAdapterList() {
        viewModelScope.launch(Dispatchers.IO) {
            utilityDataRepository.getProfessionAdapterList().collect {
                when (it.status) {
                    DataStatus.Status.Failed -> {

                    }

                    DataStatus.Status.Loading -> {

                    }

                    DataStatus.Status.Success -> {
                        it.data?.let { list -> utilityDataRepository.saveProfessionAdapterList(list) }
                    }
                }
            }
        }
    }

    val issueCategoryAdapterList: LiveData<MutableList<IssueCategory>> =
        utilityDataRepository.issueCategoryAdapterListFromDB.asLiveData()

    private fun getIssueCategoryAdapterList() {
        viewModelScope.launch(Dispatchers.IO) {
            utilityDataRepository.getIssueCategoryAdapterList().collect {
                when (it.status) {
                    DataStatus.Status.Failed -> {

                    }

                    DataStatus.Status.Loading -> {

                    }

                    DataStatus.Status.Success -> {
                        it.data?.let { list ->
                            utilityDataRepository.saveIssueCategoryAdapterList(
                                list
                            )
                        }
                    }
                }
            }
        }
    }

    private val _sendOTPToPhoneNumberResponse: MutableLiveData<DataStatus<PhoneNumberVerificationStatus?>> =
        MutableLiveData()
    val sendOTPToPhoneNumberResponse: LiveData<DataStatus<PhoneNumberVerificationStatus?>> =
        _sendOTPToPhoneNumberResponse

    fun sendOTPToPhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.sendOTPToPhoneNumber(VerifyPhoneNumber(phoneNumber)).collect {
                _sendOTPToPhoneNumberResponse.postValue(it)
            }
        }
    }

    private val _requestSupportResponse: MutableLiveData<DataStatus<RequestSupportStatus?>> =
        MutableLiveData()
    val requestSupportResponse: LiveData<DataStatus<RequestSupportStatus?>> =
        _requestSupportResponse

    fun requestSupport(requestSupport: RequestSupport) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackNSupportRepository.requestSupport(requestSupport).collect {
                _requestSupportResponse.postValue(it)
            }
        }
    }

    private val _verifyOtpStatus: MutableLiveData<DataStatus<PhoneNumberVerificationStatus?>> =
        MutableLiveData()
    val verifyOtpStatus: LiveData<DataStatus<PhoneNumberVerificationStatus?>> =
        _verifyOtpStatus

    fun verifyPhoneNumberOTP(verifyPhoneNumber: VerifyPhoneNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPhoneNumberOTP(verifyPhoneNumber).collect {
                _verifyOtpStatus.postValue(it)
            }
        }
    }

    private val _deleteUserAccountStatus: MutableLiveData<DataStatus<DeleteUserAccountStatus?>> =
        MutableLiveData()
    val deleteUserAccountStatus: LiveData<DataStatus<DeleteUserAccountStatus?>> =
        _deleteUserAccountStatus

    fun deleteUserAccount(deleteUserAccount: DeleteUserAccount) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteUserAccount(deleteUserAccount).collect {
                _deleteUserAccountStatus.postValue(it)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getProfessionAdapterList()
                getIssueCategoryAdapterList()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    getFirebaseUserToken()
                    listenForAuthStateChanges()
                }
            }

            ON_START -> {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    _isUserSignedIn.postValue(DataStatus.success(true))
                    Log.i(tagMainViewModel, "User profile uid is ${currentUser.uid}")
                } else {
                    _isUserSignedIn.postValue(DataStatus.success(false))
                }
            }

            ON_RESUME -> {
                fetchServerTime()
            }

            ON_PAUSE -> {
            }

            ON_STOP -> {
            }

            ON_DESTROY -> {
                cancelListeningToAuthState()
                cancelListeningToUserProfileChanges()
                cancelListeningToSingleDeviceLoginChanges()
            }

            ON_ANY -> {
            }
        }
    }
}
