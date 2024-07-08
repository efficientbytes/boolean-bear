package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PhoneNumberOTPVerificationViewModel :
    ViewModel(), KoinComponent {

    private val verificationRepository: VerificationRepository by inject()
    private val _verifyPhoneNumberOTPResponse: MutableLiveData<DataStatus<PhoneNumber>> =
        MutableLiveData()
    val verifyPhoneNumberOTPResponse: LiveData<DataStatus<PhoneNumber>> =
        _verifyPhoneNumberOTPResponse

    fun verifyPhoneNumberOTP(prefix: String, phoneNumber: String, otp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPhoneNumberOTP(prefix, phoneNumber, otp)
                .collect {
                    _verifyPhoneNumberOTPResponse.postValue(it)
                }
        }
    }

    private val _sendOTPToPhoneNumberResponse: MutableLiveData<DataStatus<PhoneNumber>> =
        MutableLiveData()
    val sendOTPToPhoneNumberResponse: LiveData<DataStatus<PhoneNumber>> =
        _sendOTPToPhoneNumberResponse

    fun sendOTPToPhoneNumber(prefix: String, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.sendOTPToPhoneNumber(prefix, phoneNumber)
                .collect {
                    _sendOTPToPhoneNumberResponse.postValue(it)
                }
        }
    }

}