package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.AdsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.LoginMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginOrSignUpViewModel :
    ViewModel(), KoinComponent, LifecycleEventObserver {

    private val verificationRepository: VerificationRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()
    private val adsRepository: AdsRepository by inject()
    private val authenticationRepository: AuthenticationRepository by inject()
    private val externalScope: CoroutineScope by inject()
    private val _loginMode: MutableLiveData<DataStatus<LoginMode?>?> =
        MutableLiveData()
    val loginMode: LiveData<DataStatus<LoginMode?>?> =
        _loginMode

    fun getLoginMode(prefix: String, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.getLoginMode(prefix, phoneNumber).collect {
                _loginMode.postValue(it)
            }
        }
    }

    fun resetLoginMode() {
        viewModelScope.launch(Dispatchers.IO) {
            _loginMode.postValue(null)
        }
    }

    private fun deleteAllPreviousUserData() {
        externalScope.launch {
            userProfileRepository.deleteUserProfile()
            adsRepository.deleteActiveAdsTemplate()
            authenticationRepository.deleteSingleDeviceLogin()
        }
        userProfileRepository.deleteLocalNotificationToken()
        authenticationRepository.deletePasswordCreated()
        authenticationRepository.deleteIDToken()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                deleteAllPreviousUserData()
            }

            Lifecycle.Event.ON_START -> {

            }

            Lifecycle.Event.ON_RESUME -> {

            }

            Lifecycle.Event.ON_PAUSE -> {

            }

            Lifecycle.Event.ON_STOP -> {

            }

            Lifecycle.Event.ON_DESTROY -> {

            }

            Lifecycle.Event.ON_ANY -> {

            }
        }
    }

}