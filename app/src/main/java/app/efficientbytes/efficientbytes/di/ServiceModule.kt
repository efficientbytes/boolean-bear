package app.efficientbytes.efficientbytes.di

import org.koin.dsl.module

val serviceModule = module {
    single { baseUrl }
    single { provideRetrofit(get()) }
}
