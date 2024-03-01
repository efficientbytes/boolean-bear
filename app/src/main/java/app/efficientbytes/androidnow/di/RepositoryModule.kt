package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.repositories.CourseRepository
import app.efficientbytes.androidnow.repositories.VerificationRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory { CourseRepository(get()) }
    factory { VerificationRepository(get()) }
}