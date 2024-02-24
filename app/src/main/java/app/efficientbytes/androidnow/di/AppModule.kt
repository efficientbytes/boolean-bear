package app.efficientbytes.androidnow.di

import org.koin.dsl.module

val appModule = module {
    includes(
        serviceModule, repositoryModule, viewModelModule , utilsModule
    )
}