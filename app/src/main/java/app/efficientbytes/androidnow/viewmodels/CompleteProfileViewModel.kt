package app.efficientbytes.androidnow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.repositories.UserProfileRepository
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompleteProfileViewModel(private val userProfileRepository: UserProfileRepository) :
    ViewModel() {

    private val _userProfileServerResponse: MutableLiveData<DataStatus<UserProfile?>> =
        MutableLiveData()
    val userProfileServerResponse: LiveData<DataStatus<UserProfile?>> =
        _userProfileServerResponse
    lateinit var userProfile: LiveData<DataStatus<UserProfile?>>

    fun getUserProfile(phoneNumber: String? = null, userAccountId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.getUserProfile(
                phoneNumber = phoneNumber,
                userAccountId = userAccountId
            ).collect {
                _userProfileServerResponse.postValue(it)
            }
        }
    }

    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserProfile(userProfile).collect {
                _userProfileServerResponse.postValue(it)
            }
        }
    }

}
