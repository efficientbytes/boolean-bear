package app.efficientbytes.booleanbear.di

import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository
import app.efficientbytes.booleanbear.repositories.StatisticsRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.repositories.VerificationRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory { VerificationRepository(get()) }
    factory { UserProfileRepository(get(), get(), get(), get()) }
    factory { AuthenticationRepository(get(), get(), get(), get(), get(), get()) }
    factory { UtilityDataRepository(get(), get(), get()) }
    factory { FeedbackNSupportRepository(get()) }
    factory { AssetsRepository(get(), get(), get()) }
    factory { StatisticsRepository(get(), get(), get()) }
}