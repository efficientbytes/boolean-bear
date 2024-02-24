package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.repositories.CourseRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory { CourseRepository(get()) }
}