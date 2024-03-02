package app.efficientbytes.androidnow.di

import org.koin.dsl.module

val appModule = module {
    includes(
        databaseModule, serviceModule, repositoryModule, viewModelModule, utilsModule
    )
}