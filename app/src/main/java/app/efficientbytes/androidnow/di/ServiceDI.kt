package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.services.CoursesService
import app.efficientbytes.androidnow.services.UserProfileService
import app.efficientbytes.androidnow.services.VerificationService
import app.efficientbytes.androidnow.utils.BASE_URL
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