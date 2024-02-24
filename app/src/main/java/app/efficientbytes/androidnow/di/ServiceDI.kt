package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.services.CoursesService
import app.efficientbytes.androidnow.utils.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val baseUrl = BASE_URL

fun provideRetrofit(baseUrl: String): Retrofit =
    Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
        .build()

fun provideCoursesService(retrofit: Retrofit): CoursesService =
    retrofit.create(CoursesService::class.java)
