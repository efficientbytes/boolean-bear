package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumberVerificationStatus
import app.efficientbytes.booleanbear.services.models.VerifyPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneNumberOTPVerificationViewModel(private val verificationRepository: VerificationRepository) :
    ViewModel() {

    private val _verifyPhoneNumberOTPResponse: MutableLiveData<DataStatus<PhoneNumberVerificationStatus?>> =
        MutableLiveData()
    val verifyPhoneNumberOTPResponse: LiveData<DataStatus<PhoneNumberVerificationStatus?>> =
        _verifyPhoneNumberOTPResponse

    fun verifyPhoneNumberOTP(phoneNumber: String, otp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPhoneNumberOTP(VerifyPhoneNumber(phoneNumber, otp))
                .collect {
                    _verifyPhoneNumberOTPResponse.postValue(it)
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

}