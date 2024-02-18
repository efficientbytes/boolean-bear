package app.efficientbytes.efficientbytes.di

import app.efficientbytes.efficientbytes.viewmodels.CourseViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { CourseViewModel(get()) }
}