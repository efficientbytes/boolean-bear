package app.efficientbytes.booleanbear.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single { baseUrl }
    single { provideMoshi() }
    single { provideCustomInterceptor(androidContext()) }
    single { provideOkHttpClient(get()) }
    single { provideRetrofit(get(), get(), get()) }
    single { provideVerificationService(get()) }
    single { provideUserProfileService(get()) }
    single { provideAuthenticationService(get()) }
    single { provideUtilityService(get()) }
    single { provideFeedbackNSupportService(get()) }
    single { provideAssetsService(get()) }
    single { provideStatisticsService(get()) }
    single { provideAdsService(get()) }
}
