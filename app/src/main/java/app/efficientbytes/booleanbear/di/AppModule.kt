package app.efficientbytes.booleanbear.di

import org.koin.dsl.module

val appModule = module {
    includes(
        utilsModule, databaseModule, serviceModule, repositoryModule, viewModelModule
    )
}