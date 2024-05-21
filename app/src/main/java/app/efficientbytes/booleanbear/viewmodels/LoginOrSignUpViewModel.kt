package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.LoginMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginOrSignUpViewModel(private val verificationRepository: VerificationRepository) :
    ViewModel() {

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

}