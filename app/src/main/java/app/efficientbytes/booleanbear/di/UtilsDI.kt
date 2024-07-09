package app.efficientbytes.booleanbear.di

import android.content.Context
import app.efficientbytes.booleanbear.utils.AppAuthStateListener
import app.efficientbytes.booleanbear.utils.AuthStateCoroutineScope
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.ContentDetailsLiveListener
import app.efficientbytes.booleanbear.utils.InstructorLiveListener
import app.efficientbytes.booleanbear.utils.MentionedLinksLiveListener
import app.efficientbytes.booleanbear.utils.ServiceError
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginCoroutineScope
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserAccountCoroutineScope
import app.efficientbytes.booleanbear.utils.UserProfileListener
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun provideConnectivityListener(context: Context) = ConnectivityListener(context)

fun provideReviewManager(context: Context) = ReviewManagerFactory.create(context)

private val handler = CoroutineExceptionHandler { _, _ -> }

fun provideIOCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)

fun provideUserProfileListener() = UserProfileListener

fun provideSingleDeviceLoginListener() = SingleDeviceLoginListener

fun provideAuthStateCoroutineScope() = AuthStateCoroutineScope

fun provideUserAccountCoroutineScope() = UserAccountCoroutineScope

fun provideSingleDeviceLoginCoroutineScope() = SingleDeviceLoginCoroutineScope

fun provideAppAuthStateListener() = AppAuthStateListener

fun provideServiceError() = ServiceError

fun provideInstructorLiveListener() = InstructorLiveListener()

fun provideMentionedLinksLiveListener() = MentionedLinksLiveListener()

fun provideContentDetailsLiveListener() = ContentDetailsLiveListener()