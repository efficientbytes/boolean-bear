package app.efficientbytes.androidnow.di

import android.content.Context
import app.efficientbytes.androidnow.utils.AuthStateCoroutineScope
import app.efficientbytes.androidnow.utils.ConnectivityListener
import app.efficientbytes.androidnow.utils.CustomAuthStateListener
import app.efficientbytes.androidnow.utils.SingleDeviceLoginListener
import app.efficientbytes.androidnow.utils.UserProfileListener
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