package app.efficientbytes.androidnow.viewmodels

import android.app.Application
import android.util.Log
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
import app.efficientbytes.androidnow.utils.authStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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
    private var authStateListenerJob: Job? = null
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
            val currentUser = auth.currentUser
            currentUser?.let {
                it.getIdToken(true)
                    .addOnSuccessListener { result ->
                        _firebaseUserToken.postValue(DataStatus.success(result))
                    }
            }
        }
    }

    private val _authState: MutableLiveData<Boolean> = MutableLiveData()
    val authState: LiveData<Boolean> = _authState

    fun listenForAuthStateChanges() {
        Log.i(tagMainViewModel, "Auth state listener invoked")
        Log.i(
            tagMainViewModel,
            "Auth state listener coroutine status is active? ${authStateListenerJob?.isActive}"
        )
        authStateListenerJob = viewModelScope.launch {
            auth.authStateFlow().collect { authState ->
                Log.i(tagMainViewModel, "Auth State is : $authState")
                _authState.postValue(false)
            }
        }
        Log.i(
            tagMainViewModel,
            "Auth state listener coroutine status is active? ${authStateListenerJob?.isActive}"
        )
    }

    fun cancelListeningToAuthState() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(tagMainViewModel, "Cancel listening to auth state changes function invoked")
            if (authStateListenerJob != null) {
                if (authStateListenerJob?.isActive == true) {
                    Log.i(
                        tagMainViewModel,
                        "Auth state listener coroutine status is active before cancelling ? ${authStateListenerJob?.isActive}"
                    )
                    authStateListenerJob?.cancelAndJoin()
                    Log.i(
                        tagMainViewModel,
                        "Auth state listener coroutine status is active after cancelling ? ${authStateListenerJob?.isActive}"
                    )
                    authStateListenerJob = null
                } else {
                    Log.i(
                        tagMainViewModel,
                        "Auth state listener coroutine status is not active"
                    )
                }
            }
        }
    }

    fun getUserProfile() {

    }

    fun signOutUser() {
        if (auth.currentUser != null) {
            auth.signOut()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                listenForAuthStateChanges()
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
                cancelListeningToAuthState()
            }

            ON_ANY -> {
            }
        }
    }


}