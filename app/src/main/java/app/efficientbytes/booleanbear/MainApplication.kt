package app.efficientbytes.booleanbear

import android.app.Application
import android.app.NotificationManager
import app.efficientbytes.booleanbear.di.appModule
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.utils.NotificationsHelper
import com.google.firebase.Firebase
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.initialize
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class MainApplication : Application(), KoinComponent {

    private val authenticationRepository: AuthenticationRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()
    private val utilityDataRepository: UtilityDataRepository by inject()
    private val assetsRepository: AssetsRepository by inject()

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(context = this)
        val appCheckProviderFactory: AppCheckProviderFactory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        Firebase.appCheck.installAppCheckProviderFactory(
            appCheckProviderFactory, true
        )

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
        NotificationsHelper.createNotificationChannel(
            this,
            getString(R.string.ad_free_content_countdown),
            getString(R.string.notifications_showing_the_remaining_time_for_ad_free_content_this_notification_cannot_be_dismissed_until_the_ad_free_period_ends),
            false,
            NotificationManager.IMPORTANCE_LOW
        )
        NotificationsHelper.createNotificationChannel(
            this,
            getString(R.string.ad_free_content_conclusion),
            getString(R.string.notifications_indicating_the_end_of_the_ad_free_content_period_this_notification_will_alert_you_when_your_ad_free_time_has_expired),
            false,
            NotificationManager.IMPORTANCE_HIGH,
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
            authenticationRepository.getLiveAuthStateFromRemote()
            authenticationRepository.getLiveSingleDeviceLoginFromRemote(currentUser.uid)
            userProfileRepository.getLiveUserProfileFromRemote(currentUser.uid)
        }

    }

}