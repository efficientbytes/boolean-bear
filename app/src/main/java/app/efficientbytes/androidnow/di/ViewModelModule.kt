package app.efficientbytes.androidnow.di

import app.efficientbytes.androidnow.viewmodels.AccountSettingsViewModel
import app.efficientbytes.androidnow.viewmodels.CompleteProfileViewModel
import app.efficientbytes.androidnow.viewmodels.CourseViewModel
import app.efficientbytes.androidnow.viewmodels.EditProfileFieldViewModel
import app.efficientbytes.androidnow.viewmodels.LoginOrSignUpViewModel
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import app.efficientbytes.androidnow.viewmodels.PhoneNumberOTPVerificationViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { CourseViewModel(get()) }
    viewModel { LoginOrSignUpViewModel(get()) }
    viewModel { PhoneNumberOTPVerificationViewModel(get()) }
    viewModel { CompleteProfileViewModel(get()) }
    viewModel { MainViewModel(androidApplication(), get(), get()) }
    viewModel { AccountSettingsViewModel(get()) }
    viewModel { EditProfileFieldViewModel(get(),get()) }
}