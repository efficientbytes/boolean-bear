package app.efficientbytes.booleanbear.viewmodels

import android.app.Application
import android.os.Build
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
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.database.models.ActiveAdTemplate
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.models.AdTemplate
import app.efficientbytes.booleanbear.models.IssueCategory
import app.efficientbytes.booleanbear.models.Profession
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.AdsRepository
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.RequestSupportResponse
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.SignInToken
import app.efficientbytes.booleanbear.ui.activities.MainActivity
import app.efficientbytes.booleanbear.utils.IDTokenListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.util.concurrent.TimeUnit

class MainViewModel(
    application: Application
) : AndroidViewModel(application), KoinComponent,
    LifecycleEventObserver, UtilityDataRepository.UtilityListener, IDTokenListener {

    private val authenticationRepository: AuthenticationRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()
    private val utilityDataRepository: UtilityDataRepository by inject()
    private val verificationRepository: VerificationRepository by inject()
    private val feedbackNSupportRepository: FeedbackNSupportRepository by inject()
    private val assetsRepository: AssetsRepository by inject()
    private val adsRepository: AdsRepository by inject()
    private val externalScope: CoroutineScope by inject()
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val _signInToken: MutableLiveData<DataStatus<SignInToken?>> = MutableLiveData()
    val signInToken: LiveData<DataStatus<SignInToken?>> = _signInToken
    fun getSignInToken(prefix: String, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getSignInToken(prefix, phoneNumber).collect {
                _signInToken.postValue(it)
            }
        }
    }

    private val _isUserSignedIn: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val isUserSignedIn: LiveData<DataStatus<Boolean>> = _isUserSignedIn
    fun signInWithToken(token: SignInToken) {
        viewModelScope.launch(Dispatchers.IO) {
            token.token.let {
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

    val liveUserProfileFromLocal = userProfileRepository.liveUserProfileFromLocal

    fun getLiveUserProfileFromRemote(userAccountId: String) {
        userProfileRepository.getLiveUserProfileFromRemote(userAccountId)
    }

    fun getUserProfileFromRemote() {
        userProfileRepository.getUserProfileFromRemote()
    }

    private val _singleDeviceLoginResponseFromServer: MutableLiveData<DataStatus<SingleDeviceLogin?>> =
        MutableLiveData()
    val singleDeviceLoginResponseFromServer: LiveData<DataStatus<SingleDeviceLogin?>> =
        _singleDeviceLoginResponseFromServer

    fun getRemoteSingleDeviceLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getRemoteSingleDeviceLogin().collect {
                _singleDeviceLoginResponseFromServer.postValue(it)
            }
        }
    }

    private val _singleDeviceLoginFromDB: MutableLiveData<Boolean?> = MutableLiveData()
    val singleDeviceLoginFromDB: LiveData<Boolean?> = _singleDeviceLoginFromDB
    private fun getLocalSingleDeviceLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val singleDeviceLogin = authenticationRepository.getLocalSingleDeviceLogin()
            if (currentUser != null && singleDeviceLogin == null) {
                signOutUser()
                _singleDeviceLoginFromDB.postValue(false)
            }
        }
    }

    fun resetSingleDeviceLoginFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            _singleDeviceLoginFromDB.postValue(null)
        }
    }

    val liveSingleDeviceLoginFromDB: LiveData<SingleDeviceLogin?> =
        authenticationRepository.getLiveLocalSingleDeviceLoginStatus()

    fun saveSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.saveSingleDeviceLogin(singleDeviceLogin)
        }
    }

    fun signOutUser() {
        resetUser()
        resetSingleDeviceLogin()
        resetAuth()
        resetAssets()
        FirebaseAuth.getInstance().signOut()
    }
    fun resetUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                SingletonUserData.resetInstance()
                userProfileRepository.resetUserProfileScope()
                userProfileRepository.resetUserProfileListener()
                userProfileRepository.resetUserProfileInLocal()
            }
        }
    }
    fun resetSingleDeviceLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteSingleDeviceLogin()
            authenticationRepository.resetSingleDeviceScope()
        }
    }
    fun resetAuth() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.resetAuthScope()
            authenticationRepository.deletePasswordCreated()
        }
    }
    fun resetAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.deleteCourseWaitingList()
        }
    }
    fun resetFCMToken() {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.resetLocalNotificationToken()
            userProfileRepository.resetRemoteNotificationToken()
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

    private val _sendOTPToPhoneNumberResponse: MutableLiveData<DataStatus<PhoneNumber>> =
        MutableLiveData()
    val sendOTPToPhoneNumberResponse: LiveData<DataStatus<PhoneNumber>> =
        _sendOTPToPhoneNumberResponse

    fun sendOTPToPhoneNumber(prefix: String, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.sendOTPToPhoneNumber(prefix = prefix, phoneNumber = phoneNumber)
                .collect {
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

    private val _verifyOtpStatus: MutableLiveData<DataStatus<PhoneNumber>> =
        MutableLiveData()
    val verifyOtpStatus: LiveData<DataStatus<PhoneNumber>> =
        _verifyOtpStatus

    fun verifyPhoneNumberOTP(prefix: String, phoneNumber: String, otp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPhoneNumberOTP(prefix, phoneNumber, otp).collect {
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

    fun resetDeleteUserAccountLiveData() {
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

    fun generateFCMToken() {
        userProfileRepository.generateFCMToken()
    }

    fun deleteIDToken() {
        authenticationRepository.deleteIDToken()
    }

    private fun generateIDToken() {
        authenticationRepository.generateIDToken(this@MainViewModel)
    }

    private val _preLoadRewardedAdRequested: MutableLiveData<Boolean?> = MutableLiveData()
    val preLoadRewardedAdRequested: LiveData<Boolean?> = _preLoadRewardedAdRequested

    fun preLoadRewardedAd() {
        viewModelScope.launch(Dispatchers.IO) {
            _preLoadRewardedAdRequested.postValue(true)
        }
    }

    fun resetPreLoadRewardedAd() {
        viewModelScope.launch(Dispatchers.IO) {
            _preLoadRewardedAdRequested.postValue(null)
        }
    }

    private val _preLoadingRewardedAdStatus: MutableLiveData<Boolean?> = MutableLiveData()
    val preLoadingRewardedAdStatus: LiveData<Boolean?> = _preLoadingRewardedAdStatus

    fun onPreLoadingRewardedAdStatusChanged(isSuccess: Boolean?) {
        viewModelScope.launch(Dispatchers.IO) {
            _preLoadingRewardedAdStatus.postValue(isSuccess)
        }
    }

    private val _adDisplayCompleted: MutableLiveData<Boolean?> = MutableLiveData()
    val adDisplayCompleted: LiveData<Boolean?> = _adDisplayCompleted

    fun adDisplayCompleted(isSuccess: Boolean?) {
        viewModelScope.launch(Dispatchers.IO) {
            _adDisplayCompleted.postValue(isSuccess)
        }
    }

    private val _showRewardedAds: MutableLiveData<AdTemplate?> = MutableLiveData()
    val showRewardedAds: LiveData<AdTemplate?> = _showRewardedAds

    fun showRewardedAds(adTemplate: AdTemplate?) {
        viewModelScope.launch(Dispatchers.IO) {
            _showRewardedAds.postValue(adTemplate)
        }
    }

    val getActiveAdTemplate: LiveData<ActiveAdTemplate?> = adsRepository.getActiveAdTemplate

    fun insertActiveAdTemplate(adTemplate: AdTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                System.currentTimeMillis()
            } else {
                Instant.now().toEpochMilli()
            }
            adsRepository.insertActiveAdTemplate(
                ActiveAdTemplate(
                    adTemplate.templateId,
                    true,
                    timestamp
                )
            )
        }
    }

    fun deleteActiveAdsTemplate() {
        viewModelScope.launch(Dispatchers.IO) {
            adsRepository.deleteActiveAdsTemplate()
        }
    }

    private fun crossCheckRewardedAdPauseTime() {
        viewModelScope.launch(Dispatchers.IO) {
            val activeAdTemplate = adsRepository.isAdTemplateActive()
            activeAdTemplate?.let {
                val template = AdTemplate.getPauseTimeFor(it.templateId)
                val startTimestamp = it.enabledAt
                val currentTimeStamp = System.currentTimeMillis()
                val difference = currentTimeStamp - startTimestamp
                val checkTimeInMillis = TimeUnit.MINUTES.toMillis(template.pauseTime)
                if (difference > checkTimeInMillis) {
                    deleteActiveAdsTemplate()
                } else {
                    MainActivity.isAdTemplateActive = true
                }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getProfessionalAdapterList()
                getIssueCategoriesAdapterList()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    getFirebaseUserToken()
                    getLocalSingleDeviceLogin()
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
                fetchServerTime()
                crossCheckRewardedAdPauseTime()
            }

            ON_PAUSE -> {

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

    override fun onIDTokenGenerated(token: String?) {
        if (token != null) {
            authenticationRepository.saveIDToken(IDToken(token = token))
        }
    }

    private val _waitingListCourses: MutableLiveData<DataStatus<List<String>?>> =
        MutableLiveData()
    val waitingListCourses: LiveData<DataStatus<List<String>?>> =
        _waitingListCourses

    fun getAllWaitingListCourses() {
        externalScope.launch {
            userProfileRepository.getAllWaitingListCourses().collect {
                _waitingListCourses.postValue(it)
            }
        }
    }

    fun updatePasswordCreatedFlag(value: Boolean) {
        authenticationRepository.insertPasswordCreated(value)
    }
}
