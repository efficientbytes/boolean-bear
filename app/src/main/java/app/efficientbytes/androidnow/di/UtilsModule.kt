package app.efficientbytes.androidnow.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single { provideConnectivityListener(androidContext()) }
}
