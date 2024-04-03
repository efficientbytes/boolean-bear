package app.efficientbytes.booleanbear.utils

import app.efficientbytes.booleanbear.repositories.models.AuthState
import app.efficientbytes.booleanbear.repositories.models.DataStatus
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
            trySend(AuthState.Authenticated).isSuccess
        } else {
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
                trySend(DataStatus.failed(exception.message.toString()))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists() && !snapshot.metadata.isFromCache) {
                trySend(DataStatus.success(snapshot))
            }
        }
        awaitClose {
            listener.remove()
        }
    }