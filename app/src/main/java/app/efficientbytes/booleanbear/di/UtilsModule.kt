package app.efficientbytes.booleanbear.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single { provideConnectivityListener(androidContext()) }
    single { provideReviewManager(androidContext()) }
    factory { provideIOCoroutineScope() }
    single { provideUserProfileListener() }
    single { provideSingleDeviceLoginListener() }
    single { provideAuthStateCoroutineScope() }
    single { provideCustomAuthStateListener() }
    single { provideServiceError() }
    single { provideInstructorLiveListener() }
    single { provideMentionedLinksLiveListener() }
    single { provideContentDetailsLiveListener() }
}
