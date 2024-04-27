package app.efficientbytes.booleanbear.services

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import app.efficientbytes.booleanbear.ui.activities.MainActivity
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.NotificationsHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class PushNotificationService : FirebaseMessagingService(),
    UserProfileRepository.NotificationUploadListener {

    private val userProfileRepository: UserProfileRepository by inject()
    private val connectivityListener: ConnectivityListener by inject()
    private var observer: Observer<Boolean>? = null
    private var tokenFailedToUpdate = false
    private var token: String? = null

    override fun onCreate() {
        super.onCreate()
        subscribeAllTopics()
        observer = Observer<Boolean> { data ->
            when (data) {
                true -> {
                    if (tokenFailedToUpdate) {
                        tokenFailedToUpdate = false
                        this.token?.let { token ->
                            userProfileRepository.uploadNotificationsToken(
                                token
                            )
                        }
                    }
                }

                false -> {

                }
            }
        }

        if (observer != null) {
            connectivityListener.observeForever(observer!!)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        this.token = token
        userProfileRepository.uploadNotificationsToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        if (data.isNotEmpty()) {
            val auth = data["auth"]
            val type = data["type"]
            val title = data["title"]
            val body = data["body"]
            //val handle = data["handle"]
            if (title != null && body != null) {
                val builder: NotificationCompat.Builder = when (type) {
                    "App Updates" -> {
                        NotificationsHelper.createNotifications(
                            this,
                            NotificationsHelper.channelId(this, getString(R.string.app_update)),
                            title,
                            body,
                            false
                        )
                    }

                    "Account Updates" -> {
                        NotificationsHelper.createNotifications(
                            this,
                            NotificationsHelper.channelId(this, getString(R.string.account_update)),
                            title,
                            body,
                            false
                        )

                    }

                    "Account Alerts" -> {
                        NotificationsHelper.createNotifications(
                            this,
                            NotificationsHelper.channelId(this, getString(R.string.account_alerts)),
                            title,
                            body,
                            false
                        )

                    }

                    "Watch Recommendations" -> {
                        val link = data["redirectLink"]
                        if (link != null) {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("redirectLink", link)
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                                this,
                                0,
                                intent,
                                PendingIntent.FLAG_MUTABLE
                            )

                            NotificationsHelper.createNotifications(
                                this,
                                NotificationsHelper.channelId(
                                    this,
                                    getString(R.string.watch_recommendations)
                                ),
                                title,
                                body,
                                true,
                                pendingIntent
                            )
                        } else {
                            NotificationsHelper.createNotifications(
                                this,
                                NotificationsHelper.channelId(
                                    this,
                                    getString(R.string.watch_recommendations)
                                ),
                                title,
                                body,
                                true
                            )
                        }

                    }

                    "Engagement" -> {
                        NotificationsHelper.createNotifications(
                            this,
                            NotificationsHelper.channelId(this, getString(R.string.engagements)),
                            title,
                            body,
                            true
                        )

                    }

                    else -> {
                        NotificationsHelper.createNotifications(
                            this,
                            NotificationsHelper.channelId(this, getString(R.string.app_update)),
                            title,
                            body,
                            true
                        )
                    }
                }
                NotificationsHelper.getManager(this).notify(0, builder.build())
            }
        }
    }

    override fun onTokenStatusChanged(status: DataStatus<ResponseMessage>) {
        when (status.status) {
            DataStatus.Status.NoInternet -> {
                tokenFailedToUpdate = true
            }

            else -> {

            }
        }
    }

    override fun onTokenGenerated(token: String) {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (observer != null) {
            connectivityListener.removeObserver(observer!!)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (observer != null) {
            connectivityListener.removeObserver(observer!!)
        }
    }

    private fun subscribeAllTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("updates")
    }


}