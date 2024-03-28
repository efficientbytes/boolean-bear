package app.efficientbytes.booleanbear.repositories.models

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
