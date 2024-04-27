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

class PhoneNumberOTPVerificationViewModel(private val verificationRepository: VerificationRepository) :
    ViewModel() {

    private val _verifyPhoneNumberOTPResponse: MutableLiveData<DataStatus<VerifyPhoneResponse?>> =
        MutableLiveData()
    val verifyPhoneNumberOTPResponse: LiveData<DataStatus<VerifyPhoneResponse?>> =
        _verifyPhoneNumberOTPResponse

    fun verifyPhoneNumberOTP(phoneNumber: String, otp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPhoneNumberOTP(PhoneOTP(phoneNumber, otp))
                .collect {
                    _verifyPhoneNumberOTPResponse.postValue(it)
                }
        }
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

}