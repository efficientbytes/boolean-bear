package app.efficientbytes.booleanbear.di

import app.efficientbytes.booleanbear.services.AssetsService
import app.efficientbytes.booleanbear.services.AuthenticationService
import app.efficientbytes.booleanbear.services.CoursesService
import app.efficientbytes.booleanbear.services.FeedbackNSupportService
import app.efficientbytes.booleanbear.services.UserProfileService
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.services.VerificationService
import app.efficientbytes.booleanbear.utils.BASE_URL
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

const val baseUrl = BASE_URL

fun provideMoshi(): Moshi = Moshi.Builder().build()

fun provideRetrofit(baseUrl: String, moshi: Moshi): Retrofit =
    Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshi)).baseUrl(baseUrl)
        .build()

fun provideCoursesService(retrofit: Retrofit): CoursesService =
    retrofit.create(CoursesService::class.java)

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