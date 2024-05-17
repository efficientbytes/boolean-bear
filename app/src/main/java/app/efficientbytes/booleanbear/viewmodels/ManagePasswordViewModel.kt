package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ManagePasswordViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val externalScope: CoroutineScope
) : ViewModel() {

    private val _createPassword: MutableLiveData<DataStatus<Boolean>?> = MutableLiveData()
    val createPassword: LiveData<DataStatus<Boolean>?> = _createPassword

    fun createAccountPassword(password: String) {
        externalScope.launch {
            authenticationRepository.createAccountPassword(password).collect {
                _createPassword.postValue(it)
            }
        }
    }

    fun resetCreatePasswordLiveData() {
        externalScope.launch {
            _createPassword.postValue(null)
        }
    }

    private val _updatePassword: MutableLiveData<DataStatus<Boolean>?> = MutableLiveData()
    val updatePassword: LiveData<DataStatus<Boolean>?> = _updatePassword

    fun updateAccountPassword(password: String) {
        externalScope.launch {
        }
    }

    fun resetUpdatePasswordLiveData() {
        externalScope.launch {
            _updatePassword.postValue(null)
        }
    }

}