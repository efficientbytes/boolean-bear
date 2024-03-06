package app.efficientbytes.androidnow.repositories.models

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
