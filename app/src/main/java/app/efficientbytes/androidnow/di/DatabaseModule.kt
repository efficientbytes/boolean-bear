package app.efficientbytes.androidnow.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { appDatabaseName }
    single { provideRoomDatabase(androidContext()) }
    single { provideUserProfileDao(get()) }
}