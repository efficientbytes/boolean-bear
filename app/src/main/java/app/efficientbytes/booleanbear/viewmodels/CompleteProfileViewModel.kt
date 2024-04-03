package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.UserProfilePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompleteProfileViewModel(private val userProfileRepository: UserProfileRepository) :
    ViewModel() {

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
