package app.efficientbytes.androidnow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.repositories.UserProfileRepository
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.UserProfilePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompleteProfileViewModel(private val userProfileRepository: UserProfileRepository) :
    ViewModel() {

    private val tagCompleteProfileViewModel: String = "Complete-Profile-View-Model"
    private val _userProfileServerResponse: MutableLiveData<DataStatus<UserProfilePayload?>> =
        MutableLiveData()
    val userProfileServerResponse: LiveData<DataStatus<UserProfilePayload?>> =
        _userProfileServerResponse

    fun updateUserPrivateProfileBasicDetails(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserPrivateProfileBasicDetails(userProfile).collect {
                _userProfileServerResponse.postValue(it)
            }
        }
    }

}
