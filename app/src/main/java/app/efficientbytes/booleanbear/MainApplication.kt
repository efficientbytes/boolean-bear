package app.efficientbytes.booleanbear

import android.app.Application
import app.efficientbytes.booleanbear.di.appModule
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.StatisticsRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    private val authenticationRepository: AuthenticationRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()
    private val statisticsRepository: StatisticsRepository by inject()
    private val utilityDataRepository: UtilityDataRepository by inject()
    private val assetsRepository: AssetsRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        utilityDataRepository.deleteProfessions()
        utilityDataRepository.deleteIssueCategories()
        assetsRepository.deleteAllInstructorDetails()
        if (currentUser != null) {
            authenticationRepository.listenForAuthStateChanges()
            authenticationRepository.listenToSingleDeviceLoginChange(currentUser.uid)
            userProfileRepository.listenToUserProfileChange(currentUser.uid)
            statisticsRepository.uploadPendingScreenTiming()
        } else {
            statisticsRepository.deleteUserScreenTime()
        }

    }

}