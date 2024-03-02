package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.services.CoursesService
import app.efficientbytes.androidnow.services.UserProfileService
import app.efficientbytes.androidnow.services.VerificationService
import app.efficientbytes.androidnow.utils.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val baseUrl = BASE_URL

fun provideRetrofit(baseUrl: String): Retrofit =
    Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
        .build()

fun provideCoursesService(retrofit: Retrofit): CoursesService =
    retrofit.create(CoursesService::class.java)

fun provideVerificationService(retrofit: Retrofit): VerificationService =
    retrofit.create(VerificationService::class.java)

fun provideUserProfileService(retrofit: Retrofit): UserProfileService =
    retrofit.create(UserProfileService::class.java)