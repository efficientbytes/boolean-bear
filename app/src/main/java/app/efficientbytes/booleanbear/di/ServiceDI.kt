package app.efficientbytes.booleanbear.di

import android.content.Context
import app.efficientbytes.booleanbear.database.dao.AuthenticationDao
import app.efficientbytes.booleanbear.services.AdsService
import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.AuthenticationService
import app.efficientbytes.booleanbear.services.FeedbackNSupportService
import app.efficientbytes.booleanbear.services.StatisticsService
import app.efficientbytes.booleanbear.services.UserProfileService
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.services.VerificationService
import app.efficientbytes.booleanbear.utils.AppCheckInterceptor
import app.efficientbytes.booleanbear.utils.Pi.BASE_URL
import app.efficientbytes.booleanbear.utils.NetworkInterceptor
import app.efficientbytes.booleanbear.utils.TokenInterceptor
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

const val baseUrl = BASE_URL

fun provideMoshi(): Moshi = Moshi.Builder().build()

fun provideNetworkInterceptor(context: Context): NetworkInterceptor = NetworkInterceptor(context)

fun provideAppCheckInterceptor(coroutineScope: CoroutineScope): AppCheckInterceptor =
    AppCheckInterceptor(coroutineScope)

fun provideTokenInterceptor(
    authenticationDao: AuthenticationDao,
    coroutineScope: CoroutineScope
): TokenInterceptor = TokenInterceptor(authenticationDao, coroutineScope)

fun provideOkHttpClient(
    networkInterceptor: NetworkInterceptor,
    appCheckInterceptor: AppCheckInterceptor,
    tokenInterceptor: TokenInterceptor
) = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(networkInterceptor)
    .addInterceptor(appCheckInterceptor)
    .addInterceptor(tokenInterceptor)
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

fun provideAdsService(retrofit: Retrofit): AdsService = retrofit.create(AdsService::class.java)