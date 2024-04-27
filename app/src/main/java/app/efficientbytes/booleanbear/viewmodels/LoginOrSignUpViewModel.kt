package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.VerifyPhoneResponse
import app.efficientbytes.booleanbear.services.models.PhoneOTP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginOrSignUpViewModel(private val verificationRepository: VerificationRepository) :
    ViewModel() {

    private val _sendOTPToPhoneNumberResponse: MutableLiveData<DataStatus<VerifyPhoneResponse?>?> =
        MutableLiveData()
    val sendOTPToPhoneNumberResponse: LiveData<DataStatus<VerifyPhoneResponse?>?> =
        _sendOTPToPhoneNumberResponse

    fun sendOTPToPhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.sendOTPToPhoneNumber(PhoneOTP(phoneNumber)).collect {
                _sendOTPToPhoneNumberResponse.postValue(it)
            }
        }
    }

    fun resetLiveData() {
        viewModelScope.launch(Dispatchers.IO) {
            _sendOTPToPhoneNumberResponse.postValue(null)
        }
    }

}