package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.viewmodels.CourseViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { CourseViewModel(get()) }
}