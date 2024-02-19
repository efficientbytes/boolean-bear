package app.efficientbytes.efficientbytes.di

import app.efficientbytes.efficientbytes.repositories.CourseRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory { CourseRepository(get()) }
}