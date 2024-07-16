package app.efficientbytes.booleanbear.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single { baseUrl }
    single { provideMoshi() }
    single { provideNetworkInterceptor(androidContext()) }
    single { provideAppCheckInterceptor(get()) }
    single { provideTokenInterceptor(get(), get()) }
    single { provideOkHttpClient(get(), get(), get()) }
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
