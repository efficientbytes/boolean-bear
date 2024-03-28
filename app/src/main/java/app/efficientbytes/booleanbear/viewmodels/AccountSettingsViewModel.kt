package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.UserProfileRepository

class AccountSettingsViewModel(private val userProfileRepository: UserProfileRepository) :
    ViewModel(),
    LifecycleEventObserver {

    val userProfile: LiveData<UserProfile?> = userProfileRepository.userProfile.asLiveData()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {

            }

            ON_START -> {

            }

            ON_RESUME -> {

            }

            ON_PAUSE -> {

            }

            ON_STOP -> {

            }

            ON_DESTROY -> {

            }

            ON_ANY -> {

            }
        }
    }

}