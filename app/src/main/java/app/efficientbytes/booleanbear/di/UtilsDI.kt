package app.efficientbytes.booleanbear.di

import android.content.Context
import app.efficientbytes.booleanbear.utils.AuthStateCoroutineScope
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserProfileListener
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun provideConnectivityListener(context: Context) = ConnectivityListener(context)

fun provideReviewManager(context: Context) = ReviewManagerFactory.create(context)

fun provideIOCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun provideUserProfileListener() = UserProfileListener

fun provideSingleDeviceLoginListener() = SingleDeviceLoginListener

fun provideAuthStateCoroutineScope() = AuthStateCoroutineScope

fun provideCustomAuthStateListener() = CustomAuthStateListener