package app.efficientbytes.androidnow.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.repositories.UserProfileRepository
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.UserProfilePayload
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CompleteProfileViewModel(private val userProfileRepository: UserProfileRepository) :
    ViewModel() {

    private val tagCompleteProfileViewModel: String = "Complete-Profile-View-Model"
    private val _userProfileServerResponse: MutableLiveData<DataStatus<UserProfilePayload?>> =
        MutableLiveData()
    val userProfileServerResponse: LiveData<DataStatus<UserProfilePayload?>> =
        _userProfileServerResponse

    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserProfile(userProfile).collect {
                Log.i(tagCompleteProfileViewModel, "User profile payload is ${it.data}")
                _userProfileServerResponse.postValue(it)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveUserProfile(userProfile: UserProfile) {
        GlobalScope.launch {
            userProfileRepository.saveUserProfile(userProfile)
        }
    }

}
