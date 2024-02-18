package app.efficientbytes.efficientbytes.di

import org.koin.dsl.module

val appModule = module {
    includes(
        serviceModule, repositoryModule, viewModelModule
    )
}