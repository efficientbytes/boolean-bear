package app.efficientbytes.androidnow.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.repositories.AuthenticationRepository
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.services.models.SignInToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application,
    private val authenticationRepository: AuthenticationRepository
) : AndroidViewModel(application),
    LifecycleEventObserver {

    private val tagMainViewModel: String = "Main View Model"
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val _isUserSignedIn: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val isUserSignedIn: LiveData<DataStatus<Boolean>> = _isUserSignedIn
    private val _signInToken: MutableLiveData<DataStatus<SignInToken?>> = MutableLiveData()
    val signInToken: LiveData<DataStatus<SignInToken?>> = _signInToken
    private val _firebaseUserToken: MutableLiveData<DataStatus<GetTokenResult>> = MutableLiveData()
    val firebaseUserToken: LiveData<DataStatus<GetTokenResult>> = _firebaseUserToken

    fun getSignInToken(phoneNumber: PhoneNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getSignInToken(phoneNumber).collect {
                _signInToken.postValue(it)
            }
        }
    }

    fun signInWithToken(token: SignInToken) {
        viewModelScope.launch(Dispatchers.IO) {
            token.token?.let {
                auth.signInWithCustomToken(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isUserSignedIn.postValue(DataStatus.success(true))
                        } else _isUserSignedIn.postValue(DataStatus.failed(task.exception?.message.toString()))
                    }.addOnFailureListener { exception ->
                        _isUserSignedIn.postValue(DataStatus.failed(exception.message.toString()))
                    }
            }
        }
    }

    fun getFirebaseUserToken(refresh: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.auth.currentUser?.getIdToken(refresh)
                ?.addOnSuccessListener { result ->
                    _firebaseUserToken.postValue(DataStatus.success(result))
                }?.addOnFailureListener {
                    _firebaseUserToken.postValue(DataStatus.failed(it.message.toString()))
                }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {

            }

            ON_START -> {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    _isUserSignedIn.postValue(DataStatus.success(true))
                } else {
                    _isUserSignedIn.postValue(DataStatus.success(false))
                }
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