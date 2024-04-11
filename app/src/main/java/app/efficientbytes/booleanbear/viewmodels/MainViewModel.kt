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
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository
import app.efficientbytes.booleanbear.repositories.StatisticsRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.DeleteUserAccount
import app.efficientbytes.booleanbear.services.models.DeleteUserAccountStatus
import app.efficientbytes.booleanbear.services.models.IssueCategory
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import app.efficientbytes.booleanbear.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.booleanbear.services.models.Profession
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.RequestSupportStatus
import app.efficientbytes.booleanbear.services.models.SignInToken
import app.efficientbytes.booleanbear.services.models.VerifyPhoneNumber
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
    private val externalScope: CoroutineScope
) : AndroidViewModel(application),
    LifecycleEventObserver {

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

    fun signOutUser() {
        if (auth.currentUser != null) {
            statisticsRepository.noteDownScreenClosingTime()
            statisticsRepository.forceUploadPendingScreenTiming()
            auth.signOut()
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

    val professionAdapterListFromDB: LiveData<MutableList<Profession>> =
        utilityDataRepository.professionAdapterListFromDB.asLiveData()
    private val _professionalAdapterList: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val professionalAdapterList: LiveData<DataStatus<Boolean>> = _professionalAdapterList

    fun getProfessionalAdapterList() {
        externalScope.launch {
            utilityDataRepository.getProfessionAdapterList().collect {
                _professionalAdapterList.postValue(it)
            }
        }
    }

    private val _issueCategoriesAdapter: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val issueCategoriesAdapter: LiveData<DataStatus<Boolean>> = _issueCategoriesAdapter

    fun getIssueCategoriesAdapterList() {
        externalScope.launch {
            utilityDataRepository.getIssueCategoryAdapterList().collect {
                _issueCategoriesAdapter.postValue(it)
            }
        }
    }

    val issueCategoryAdapterListFromDB: LiveData<MutableList<IssueCategory>> =
        utilityDataRepository.issueCategoryAdapterListFromDB.asLiveData()
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
                statisticsRepository.noteDownScreenOpeningTime()
                fetchServerTime()
            }

            ON_PAUSE -> {
                statisticsRepository.noteDownScreenClosingTime()
            }

            ON_STOP -> {
                assetsRepository.deleteAllContents()
            }

            ON_DESTROY -> {

            }

            ON_ANY -> {
            }
        }
    }
}
