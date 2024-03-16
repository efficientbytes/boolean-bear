package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.repositories.AuthenticationRepository
import app.efficientbytes.androidnow.repositories.CourseRepository
import app.efficientbytes.androidnow.repositories.UserProfileRepository
import app.efficientbytes.androidnow.repositories.UtilityDataRepository
import app.efficientbytes.androidnow.repositories.VerificationRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory { CourseRepository(get()) }
    factory { VerificationRepository(get()) }
    factory { UserProfileRepository(get(), get()) }
    factory { AuthenticationRepository(get(), get()) }
    factory { UtilityDataRepository(get(), get()) }
}