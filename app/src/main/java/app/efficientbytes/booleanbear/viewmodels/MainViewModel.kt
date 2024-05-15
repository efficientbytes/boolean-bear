package app.efficientbytes.booleanbear.viewmodels

import android.app.Application
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
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.database.models.LocalNotificationToken
import app.efficientbytes.booleanbear.models.IssueCategory
import app.efficientbytes.booleanbear.models.Profession
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.SingletonPreviousUserId
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository
import app.efficientbytes.booleanbear.repositories.StatisticsRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import app.efficientbytes.booleanbear.services.models.PhoneOTP
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.RequestSupportResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.SignInToken
import app.efficientbytes.booleanbear.services.models.VerifyPhoneResponse
import app.efficientbytes.booleanbear.utils.IDTokenListener
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginCoroutineScope
import app.efficientbytes.booleanbear.utils.UserAccountCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainViewModel(
    private val application: Application,
    private val authenticationRepository: AuthenticationRepository,
    private val userProfileRepository: UserProfileRepository,
    private val utilityDataRepository: UtilityDataRepository,
    private val verificationRepository: VerificationRepository,
    private val feedbackNSupportRepository: FeedbackNSupportRepository,
    private val statisticsRepository: StatisticsRepository,
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope,
    private val userAccountCoroutineScope: UserAccountCoroutineScope,
    private val singleDeviceLoginCoroutineScope: SingleDeviceLoginCoroutineScope,
) : AndroidViewModel(application),
    LifecycleEventObserver, UtilityDataRepository.UtilityListener,
    UserProfileRepository.NotificationUploadListener, IDTokenListener {

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
                        _firebaseUserToken.postValue(DataStatus.success(result))
                    }
            }
        }
    }

    val listenToUserProfileFromDB = userProfileRepository.userProfile.asLiveData()

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

    val singleDeviceLoginResponseFromDB: LiveData<SingleDeviceLogin?> =
        authenticationRepository.singleDeviceLoginResponseFromDB.asLiveData()
    private val _singleDeviceLoginResponseFromServer: MutableLiveData<DataStatus<SingleDeviceLogin?>> =
        MutableLiveData()
    val singleDeviceLoginResponseFromServer: LiveData<DataStatus<SingleDeviceLogin?>> =
        _singleDeviceLoginResponseFromServer

    fun getSingleDeviceLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getSingleDeviceLogin().collect {
                _singleDeviceLoginResponseFromServer.postValue(it)
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

    fun signOutUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            SingletonPreviousUserId.setInstance(currentUser.uid)
            assetsRepository.deleteCourseWaitingList()
            userProfileRepository.resetUserProfileScope()
            authenticationRepository.resetSingleDeviceScope()
            statisticsRepository.noteDownScreenClosingTime()
            statisticsRepository.forceUploadPendingScreenTiming()
            authenticationRepository.resetAuthScope()
            FirebaseAuth.getInstance().signOut()
        }
    }

    private val _serverTime =
        MutableLiveData<DataStatus<Long?>>()
    val serverTime: LiveData<DataStatus<Long?>> = _serverTime
    fun fetchServerTime() {
        externalScope.launch(Dispatchers.IO) {
            _serverTime.postValue(DataStatus.loading())
            val timeServer = "time.google.com"
            val client = NTPUDPClient()
            client.defaultTimeout = 10_000
            try {
                val inetAddress = InetAddress.getByName(timeServer)
                val timeInfo: TimeInfo = client.getTime(inetAddress)
                val time = timeInfo.message.receiveTimeStamp.time
                _serverTime.postValue(DataStatus.success(time))
            } catch (e: Exception) {
                when {
                    e is UnknownHostException -> {
                        _serverTime.postValue(DataStatus.noInternet())
                    }

                    e is SocketTimeoutException -> {
                        _serverTime.postValue(DataStatus.timeOut())
                    }

                    else -> {
                        _serverTime.postValue(DataStatus.unknownException(e.message.toString()))
                    }
                }
            } finally {
                client.close()
            }
        }
    }

    private val _professionalAdapterList: MutableLiveData<DataStatus<List<Profession>>> =
        MutableLiveData()
    val professionalAdapterList: LiveData<DataStatus<List<Profession>>> = _professionalAdapterList

    fun getProfessionalAdapterList() {
        utilityDataRepository.getProfessionsAdapterList(this@MainViewModel)
    }

    private val _issueCategoriesAdapter: MutableLiveData<DataStatus<List<IssueCategory>>> =
        MutableLiveData()
    val issueCategoriesAdapter: LiveData<DataStatus<List<IssueCategory>>> = _issueCategoriesAdapter

    fun getIssueCategoriesAdapterList() {
        utilityDataRepository.getIssueCategoriesAdapterList(this@MainViewModel)
    }

    private val _sendOTPToPhoneNumberResponse: MutableLiveData<DataStatus<VerifyPhoneResponse?>> =
        MutableLiveData()
    val sendOTPToPhoneNumberResponse: LiveData<DataStatus<VerifyPhoneResponse?>> =
        _sendOTPToPhoneNumberResponse

    fun sendOTPToPhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.sendOTPToPhoneNumber(PhoneOTP(phoneNumber)).collect {
                _sendOTPToPhoneNumberResponse.postValue(it)
            }
        }
    }

    private val _requestSupportResponse: MutableLiveData<DataStatus<RequestSupportResponse?>> =
        MutableLiveData()
    val requestSupportResponse: LiveData<DataStatus<RequestSupportResponse?>> =
        _requestSupportResponse

    fun requestSupport(requestSupport: RequestSupport) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackNSupportRepository.requestSupport(requestSupport).collect {
                _requestSupportResponse.postValue(it)
            }
        }
    }

    private val _verifyOtpStatus: MutableLiveData<DataStatus<VerifyPhoneResponse?>> =
        MutableLiveData()
    val verifyOtpStatus: LiveData<DataStatus<VerifyPhoneResponse?>> =
        _verifyOtpStatus

    fun verifyPhoneNumberOTP(phoneOTP: PhoneOTP) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPhoneNumberOTP(phoneOTP).collect {
                _verifyOtpStatus.postValue(it)
            }
        }
    }

    private val _deleteUserAccountStatus: MutableLiveData<DataStatus<ResponseMessage?>?> =
        MutableLiveData()
    val deleteUserAccountStatus: LiveData<DataStatus<ResponseMessage?>?> =
        _deleteUserAccountStatus

    fun deleteUserAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteUserAccount().collect {
                _deleteUserAccountStatus.postValue(it)
            }
        }
    }

    fun resetDeleteUserAccountLiveData(){
        externalScope.launch {
            _deleteUserAccountStatus.postValue(null)
        }
    }

    private val _watchContentIntentInvoked: MutableLiveData<String?> =
        MutableLiveData()
    val watchContentIntentInvoked: LiveData<String?> =
        _watchContentIntentInvoked

    fun watchContentIntent(contentId: String) {
        _watchContentIntentInvoked.postValue(contentId)
    }

    fun resetWatchContentIntentInvoked() {
        _watchContentIntentInvoked.postValue(null)
    }

    private val _deleteAccountIntentInvoked: MutableLiveData<Boolean?> =
        MutableLiveData()
    val deleteAccountIntentInvoked: LiveData<Boolean?> =
        _deleteAccountIntentInvoked

    fun deleteAccountIntent() {
        _deleteAccountIntentInvoked.postValue(true)
    }

    fun resetDeleteAccountIntentInvoked() {
        _deleteAccountIntentInvoked.postValue(null)
    }

    private val _notificationStatusChanged: MutableLiveData<DataStatus<ResponseMessage>> =
        MutableLiveData()
    val notificationStatusChanged: LiveData<DataStatus<ResponseMessage>> =
        _notificationStatusChanged

    fun generateFCMToken() {
        userProfileRepository.generateFCMToken(this@MainViewModel)
    }

    fun deleteFCMToken() {
        userProfileRepository.deleteLocalNotificationToken()
        userProfileRepository.deleteRemoteNotificationToken()
    }

    fun deleteIDToken() {
        authenticationRepository.deleteIDToken()
    }

    private fun generateIDToken() {
        authenticationRepository.generateIDToken(this@MainViewModel)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getProfessionalAdapterList()
                getIssueCategoriesAdapterList()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    getFirebaseUserToken()
                }
            }

            ON_START -> {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    _isUserSignedIn.postValue(DataStatus.success(true))
                } else {
                    _isUserSignedIn.postValue(DataStatus.success(false))
                }
            }

            ON_RESUME -> {
                generateIDToken()
                statisticsRepository.noteDownScreenOpeningTime()
                fetchServerTime()
            }

            ON_PAUSE -> {
                statisticsRepository.noteDownScreenClosingTime()
            }

            ON_STOP -> {

            }

            ON_DESTROY -> {

            }

            ON_ANY -> {
            }
        }
    }

    override fun onProfessionsAdapterListStatusChanged(status: DataStatus<List<Profession>>) {
        _professionalAdapterList.postValue(status)
    }

    override fun onIssueCategoriesAdapterListStatusChanged(status: DataStatus<List<IssueCategory>>) {
        _issueCategoriesAdapter.postValue(status)
    }

    override fun onTokenStatusChanged(status: DataStatus<ResponseMessage>) {
        _notificationStatusChanged.postValue(status)
    }

    override fun onTokenGenerated(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userProfileRepository.saveNotificationToken(
                LocalNotificationToken(
                    token,
                    currentUser.uid
                )
            )
            userProfileRepository.uploadNotificationsToken(token, this@MainViewModel)
        }
    }

    override fun onIDTokenGenerated(token: String?) {
        if (token != null) {
            authenticationRepository.saveIDToken(IDToken(token = token))
        }
    }
}
