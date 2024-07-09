package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountSettingsViewModel :
    ViewModel(), KoinComponent {

    private val userProfileRepository: UserProfileRepository by inject()
    val userProfile: LiveData<UserProfile?> = userProfileRepository.liveUserProfileFromLocal

}