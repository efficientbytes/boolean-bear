package app.efficientbytes.booleanbear.di

import android.content.Context
import android.util.Log
import app.efficientbytes.booleanbear.utils.AuthStateCoroutineScope
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.ServiceError
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.UtilityCoroutineScope
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun provideConnectivityListener(context: Context) = ConnectivityListener(context)

fun provideReviewManager(context: Context) = ReviewManagerFactory.create(context)

private val handler = CoroutineExceptionHandler { _, exception ->
    Log.i("Common Scope", exception.message.toString())
}

fun provideIOCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)

fun provideUserProfileListener() = UserProfileListener

fun provideSingleDeviceLoginListener() = SingleDeviceLoginListener

fun provideAuthStateCoroutineScope() = AuthStateCoroutineScope

fun provideCustomAuthStateListener() = CustomAuthStateListener

fun provideServiceError() = ServiceError
fun provideUtilityCoroutineScope() = UtilityCoroutineScope

