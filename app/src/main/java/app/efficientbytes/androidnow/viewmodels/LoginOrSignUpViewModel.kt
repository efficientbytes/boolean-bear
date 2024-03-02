package app.efficientbytes.androidnow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.repositories.VerificationRepository
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.androidnow.services.models.VerifyPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginOrSignUpViewModel(private val verificationRepository: VerificationRepository) :
    ViewModel() {

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

}