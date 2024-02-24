package app.efficientbytes.androidnow.di

import org.koin.dsl.module

val serviceModule = module {
    single { baseUrl }
    single { provideRetrofit(get()) }
    single { provideCoursesService(get()) }
}
