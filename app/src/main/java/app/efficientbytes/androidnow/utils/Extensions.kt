package app.efficientbytes.androidnow.utils

import android.util.Log
import app.efficientbytes.androidnow.repositories.models.AuthState
import app.efficientbytes.androidnow.repositories.models.DataStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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

fun DocumentReference.addSnapshotListenerFlow(): Flow<DataStatus<DocumentSnapshot?>> =
    callbackFlow {
        val listener = addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.i(
                    "Snapshot listener",
                    "Exception occurred,${exception.localizedMessage}"
                )
                trySend(DataStatus.failed(exception.message.toString()))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists() && !snapshot.metadata.isFromCache) {
                // The document has data
                Log.i(
                    "Snapshot listener",
                    "User profile updated."
                )
                trySend(DataStatus.success(snapshot))
            } else {
                Log.i("Snapshot listener", "Document does not exist")
                trySend(DataStatus.failed("User profile does not exist"))
            }
        }
        awaitClose {
            listener.remove()
        }
    }