package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CompleteProfileViewModel :
    ViewModel(), KoinComponent {

    private val userProfileRepository: UserProfileRepository by inject()
    private val _userProfileServerResponse: MutableLiveData<DataStatus<UserProfile?>> =
        MutableLiveData()
    val userProfileServerResponse: LiveData<DataStatus<UserProfile?>> =
        _userProfileServerResponse

    fun updateUserPrivateProfileBasicDetails(userProfile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserPrivateProfileBasicDetails(userProfile).collect {
                _userProfileServerResponse.postValue(it)
            }
        }
    }

}
