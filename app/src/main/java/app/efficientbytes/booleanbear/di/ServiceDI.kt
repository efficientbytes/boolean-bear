package app.efficientbytes.booleanbear.di

import android.content.Context
import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.AuthenticationService
import app.efficientbytes.booleanbear.services.FeedbackNSupportService
import app.efficientbytes.booleanbear.services.StatisticsService
import app.efficientbytes.booleanbear.services.UserProfileService
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.services.VerificationService
import app.efficientbytes.booleanbear.utils.BASE_URL
import app.efficientbytes.booleanbear.utils.CustomInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

const val baseUrl = BASE_URL

fun provideMoshi(): Moshi = Moshi.Builder().build()

fun provideCustomInterceptor(context: Context): CustomInterceptor = CustomInterceptor(context)

fun provideOkHttpClient(customInterceptor: CustomInterceptor) = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(customInterceptor)
    .build()

fun provideRetrofit(baseUrl: String, moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
    Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshi)).baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

fun provideVerificationService(retrofit: Retrofit): VerificationService =
    retrofit.create(VerificationService::class.java)

fun provideUserProfileService(retrofit: Retrofit): UserProfileService =
    retrofit.create(UserProfileService::class.java)

fun provideAuthenticationService(retrofit: Retrofit): AuthenticationService =
    retrofit.create(AuthenticationService::class.java)

fun provideUtilityService(retrofit: Retrofit): UtilityDataService =
    retrofit.create(UtilityDataService::class.java)

fun provideFeedbackNSupportService(retrofit: Retrofit): FeedbackNSupportService =
    retrofit.create(FeedbackNSupportService::class.java)

fun provideAssetsService(retrofit: Retrofit): AssetsService =
    retrofit.create(AssetsService::class.java)

fun provideStatisticsService(retrofit: Retrofit): StatisticsService =
    retrofit.create(StatisticsService::class.java)