package app.efficientbytes.booleanbear.di

import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single { provideConnectivityListener(androidContext()) }
    single { provideReviewManager(androidContext()) }
    factory { provideIOCoroutineScope() }
    single { provideUserProfileListener() }
    single { provideSingleDeviceLoginListener() }
    single { provideAuthStateCoroutineScope() }
    single { provideUserAccountCoroutineScope() }
    single { provideSingleDeviceLoginCoroutineScope() }
    single { provideCustomAuthStateListener() }
    single { provideServiceError() }
    single { provideInstructorLiveListener() }
    single { provideMentionedLinksLiveListener() }
    single { provideContentDetailsLiveListener() }
    single { WorkManager.getInstance(get()) }
}
