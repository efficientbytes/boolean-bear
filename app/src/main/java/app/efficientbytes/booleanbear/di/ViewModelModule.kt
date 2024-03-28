package app.efficientbytes.booleanbear.di

import app.efficientbytes.booleanbear.viewmodels.AccountSettingsViewModel
import app.efficientbytes.booleanbear.viewmodels.CompleteProfileViewModel
import app.efficientbytes.booleanbear.viewmodels.CourseViewModel
import app.efficientbytes.booleanbear.viewmodels.EditProfileFieldViewModel
import app.efficientbytes.booleanbear.viewmodels.LoginOrSignUpViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import app.efficientbytes.booleanbear.viewmodels.PhoneNumberOTPVerificationViewModel
import app.efficientbytes.booleanbear.viewmodels.ShareFeedbackViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { CourseViewModel(get()) }
    viewModel { LoginOrSignUpViewModel(get()) }
    viewModel { PhoneNumberOTPVerificationViewModel(get()) }
    viewModel { CompleteProfileViewModel(get()) }
    viewModel { MainViewModel(androidApplication(), get(), get(), get(), get(), get()) }
    viewModel { AccountSettingsViewModel(get()) }
    viewModel { EditProfileFieldViewModel(get(), get()) }
    viewModel { ShareFeedbackViewModel(get()) }
}