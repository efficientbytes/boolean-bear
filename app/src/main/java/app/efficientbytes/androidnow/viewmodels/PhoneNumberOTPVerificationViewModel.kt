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

}