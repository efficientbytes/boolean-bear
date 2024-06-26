package app.efficientbytes.booleanbear

import android.app.Application
import android.app.NotificationManager
import app.efficientbytes.booleanbear.di.appModule
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.utils.NotificationsHelper
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    private val authenticationRepository: AuthenticationRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()
    private val utilityDataRepository: UtilityDataRepository by inject()
    private val assetsRepository: AssetsRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }
        NotificationsHelper.createNotificationChannel(
            this, getString(R.string.app_update),
            getString(R.string.app_update_description),
            enableVibration = true
        )
        NotificationsHelper.createNotificationChannel(
            this, getString(R.string.account_update),
            getString(R.string.account_update_description)
        )
        NotificationsHelper.createNotificationChannel(
            this, getString(R.string.account_alerts),
            getString(R.string.account_alert_description),
            importance = NotificationManager.IMPORTANCE_HIGH,
            enableLights = true,
            enableVibration = true
        )
        NotificationsHelper.createNotificationChannel(
            this, getString(R.string.watch_recommendations),
            getString(R.string.watch_recommendations_description),
            enableVibration = true
        )
        NotificationsHelper.createNotificationChannel(
            this, getString(R.string.engagements),
            getString(R.string.engagement_description),
            enableVibration = true
        )
        val currentUser = FirebaseAuth.getInstance().currentUser
        assetsRepository.deleteReelTopics()
        assetsRepository.deleteReels()
        utilityDataRepository.deleteProfessions()
        utilityDataRepository.deleteIssueCategories()
        assetsRepository.deleteAllInstructorDetails()
        assetsRepository.deleteAllMentionedLinks()
        assetsRepository.deleteCourseTopics()
        assetsRepository.deleteCourses()
        if (currentUser != null) {
            authenticationRepository.listenForAuthStateChanges()
            userProfileRepository.listenToUserProfileChange(currentUser.uid)
            authenticationRepository.listenToSingleDeviceLoginChange(currentUser.uid)
        } else {
            userProfileRepository.deleteLocalNotificationToken()
            authenticationRepository.deleteIDToken()
            assetsRepository.deleteCourseWaitingList()
        }

    }

}