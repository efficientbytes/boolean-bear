package app.efficientbytes.booleanbear.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { appDatabaseName }
    single { provideRoomDatabase(androidContext()) }
    single { provideUserProfileDao(get()) }
    single { provideAuthenticationDao(get()) }
    single { provideUtilityDataDao(get()) }
    single { provideAssetsDao(get()) }
    single { provideStatisticsDao(get()) }
}