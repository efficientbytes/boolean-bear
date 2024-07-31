package app.efficientbytes.booleanbear.di

import app.efficientbytes.booleanbear.viewmodels.AccountSettingsViewModel
import app.efficientbytes.booleanbear.viewmodels.CompleteProfileViewModel
import app.efficientbytes.booleanbear.viewmodels.CourseWaitingListViewModel
import app.efficientbytes.booleanbear.viewmodels.DiscoverViewModel
import app.efficientbytes.booleanbear.viewmodels.EditProfileFieldViewModel
import app.efficientbytes.booleanbear.viewmodels.HomeViewModel
import app.efficientbytes.booleanbear.viewmodels.ListReelViewModel
import app.efficientbytes.booleanbear.viewmodels.LoginOrSignUpViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import app.efficientbytes.booleanbear.viewmodels.ManagePasswordViewModel
import app.efficientbytes.booleanbear.viewmodels.PhoneNumberOTPVerificationViewModel
import app.efficientbytes.booleanbear.viewmodels.ReelPlayerViewModel
import app.efficientbytes.booleanbear.viewmodels.ShareFeedbackViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(
            androidApplication()
        )
    }
    viewModel { HomeViewModel() }
    viewModel { LoginOrSignUpViewModel() }
    viewModel { PhoneNumberOTPVerificationViewModel() }
    viewModel { CompleteProfileViewModel() }
    viewModel { AccountSettingsViewModel() }
    viewModel { EditProfileFieldViewModel() }
    viewModel { ShareFeedbackViewModel() }
    viewModel { ReelPlayerViewModel() }
    viewModel { DiscoverViewModel() }
    viewModel { ListReelViewModel() }
    viewModel { CourseWaitingListViewModel(get()) }
    viewModel { ManagePasswordViewModel() }
}