package app.efficientbytes.booleanbear.di

import org.koin.dsl.module

val serviceModule = module {
    single { baseUrl }
    single { provideMoshi() }
    single { provideRetrofit(get(), get()) }
    single { provideCoursesService(get()) }
    single { provideVerificationService(get()) }
    single { provideUserProfileService(get()) }
    single { provideAuthenticationService(get()) }
    single { provideUtilityService(get()) }
    single { provideFeedbackNSupportService(get()) }
    single { provideAssetsService(get()) }
}
