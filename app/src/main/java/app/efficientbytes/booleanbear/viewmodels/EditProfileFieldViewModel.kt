package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.services.models.VerifyPrimaryEmailAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditProfileFieldViewModel :
    ViewModel(), KoinComponent {

    private val userProfileRepository: UserProfileRepository by inject()
    private val verificationRepository: VerificationRepository by inject()
    private val _userProfileServerResponse: MutableLiveData<DataStatus<UserProfile?>> =
        MutableLiveData()
    val userProfileServerResponse: LiveData<DataStatus<UserProfile?>> =
        _userProfileServerResponse

    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserPrivateProfile(userProfile).collect {
                _userProfileServerResponse.postValue(it)
            }
        }
    }

    private val _primaryEmailAddressVerificationServerResponse: MutableLiveData<DataStatus<ResponseMessage?>?> =
        MutableLiveData()
    val primaryEmailAddressVerificationServerStatus: LiveData<DataStatus<ResponseMessage?>?> =
        _primaryEmailAddressVerificationServerResponse

    fun sendVerificationLinkToPrimaryEmailAddress(verifyPrimaryEmailAddress: VerifyPrimaryEmailAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPrimaryEmailAddress(verifyPrimaryEmailAddress).collect {
                _primaryEmailAddressVerificationServerResponse.postValue(it)
            }
        }
    }

    fun resetPrimaryEmailAddress() {
        viewModelScope.launch {
            _primaryEmailAddressVerificationServerResponse.postValue(null)
        }
    }
}
