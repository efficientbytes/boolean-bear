package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PrimaryEmailAddressVerificationStatus
import app.efficientbytes.booleanbear.services.models.UserProfilePayload
import app.efficientbytes.booleanbear.services.models.VerifyPrimaryEmailAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileFieldViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val verificationRepository: VerificationRepository
) :
    ViewModel() {

    private val tagEditProfileFieldViewModel: String = "Edit-Profile-Field-View-Model"
    private val _userProfileServerResponse: MutableLiveData<DataStatus<UserProfilePayload?>> =
        MutableLiveData()
    val userProfileServerResponse: LiveData<DataStatus<UserProfilePayload?>> =
        _userProfileServerResponse

    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserPrivateProfile(userProfile).collect {
                _userProfileServerResponse.postValue(it)
            }
        }
    }

    private val _primaryEmailAddressVerificationServerResponse: MutableLiveData<DataStatus<PrimaryEmailAddressVerificationStatus?>> =
        MutableLiveData()
    val primaryEmailAddressVerificationServerStatus: LiveData<DataStatus<PrimaryEmailAddressVerificationStatus?>> =
        _primaryEmailAddressVerificationServerResponse

    fun sendVerificationLinkToPrimaryEmailAddress(verifyPrimaryEmailAddress: VerifyPrimaryEmailAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyPrimaryEmailAddress(verifyPrimaryEmailAddress).collect {
                _primaryEmailAddressVerificationServerResponse.postValue(it)
            }
        }
    }
}
