package app.efficientbytes.androidnow.utils

import android.util.Log
import app.efficientbytes.androidnow.repositories.models.AuthState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun FirebaseAuth.authStateFlow(): Flow<AuthState> = callbackFlow {
    val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            Log.i("Extension - Firebase Auth", "Current User is : ${user.uid}")
            trySend(AuthState.Authenticated).isSuccess
        } else {
            Log.i("Extension - Firebase Auth", "No user is signed in.")
            trySend(AuthState.Unauthenticated).isFailure
        }
    }
    addAuthStateListener(authListener)
    awaitClose {
        removeAuthStateListener(authListener)
    }
}