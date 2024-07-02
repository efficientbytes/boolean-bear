package app.efficientbytes.booleanbear.services

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.utils.NotificationsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AdFreeSessionService : LifecycleService() {

    companion object {

        const val EXTRA_PAUSE_DURATION_IN_MILLIS = "extra_pause_duration_in_millis"
        const val EXTRA_CONCLUSION_MESSAGE = "extra_conclusion_message"
        const val EXTRA_PAUSE_DURATION_IN_MINUTES = "extra_pause_duration_in_minutes"
        const val PROGRESS_NOTIFICATION_ID = 1
        const val CONCLUSION_NOTIFICATION_ID = 2

    }

    enum class IntentAction {
        START_SERVICE,
        STOP_SERVICE
    }

    private var isServiceRunning = false
    private var pauseDurationInMinutes: Long = 0L
    private var pauseDurationInMillis: Long = 0L
    private var conclusionMessage: String? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            IntentAction.START_SERVICE.toString() -> {
                if (isServiceRunning) {
                    isServiceRunning = false
                    NotificationManagerCompat.from(this).cancel(PROGRESS_NOTIFICATION_ID)
                    stopSelf()
                    return START_NOT_STICKY
                }
                pauseDurationInMinutes = intent.getLongExtra(EXTRA_PAUSE_DURATION_IN_MINUTES, 0L)
                conclusionMessage = intent.getStringExtra(EXTRA_CONCLUSION_MESSAGE)
                pauseDurationInMillis = TimeUnit.MINUTES.toMillis(pauseDurationInMinutes)
                try {
                    startCountdown()
                    startForeground(
                        PROGRESS_NOTIFICATION_ID,
                        createTimeRemainingNotification().build()
                    )
                } catch (e: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException
                    ) {
                    }
                }
            }

            IntentAction.STOP_SERVICE.toString() -> {
                NotificationManagerCompat.from(this).cancel(PROGRESS_NOTIFICATION_ID)
                isServiceRunning = false
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun startCountdown() {
        lifecycleScope.launch(Dispatchers.IO) {
            var timeLeft = pauseDurationInMillis
            while (timeLeft > 0) {
                updateTimeRemainingNotification(timeLeft)
                delay(950)
                timeLeft -= 1000
            }
            NotificationManagerCompat.from(this@AdFreeSessionService)
                .cancel(PROGRESS_NOTIFICATION_ID)
            showConclusionNotification()
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun createTimeRemainingNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            this,
            NotificationsHelper.channelId(this, getString(R.string.ad_free_content_countdown))
        )
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.app_logo_type_rounded_corners_no_name_black_backgroundboolean_bear_logo
                )
            )
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Ad-Free session remaining")
            .setContentText("${pauseDurationInMillis / 1000 / 60} minutes remaining")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(pauseDurationInMillis.toInt(), pauseDurationInMillis.toInt(), false)

    }

    private fun updateTimeRemainingNotification(timeLeftMillis: Long) {
        val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis)
        val notification = createTimeRemainingNotification()
            .setContentText("$minutesLeft minutes remaining")
            .setProgress(pauseDurationInMillis.toInt(), timeLeftMillis.toInt(), false)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(PROGRESS_NOTIFICATION_ID, notification)
    }

    private fun showConclusionNotification() {
        val notification = NotificationCompat.Builder(
            this,
            NotificationsHelper.channelId(this, getString(R.string.ad_free_content_conclusion))
        )
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.app_logo_type_rounded_corners_no_name_black_backgroundboolean_bear_logo
                )
            )
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Ad-Free session is over")
            .setContentText(conclusionMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(CONCLUSION_NOTIFICATION_ID, notification)
    }

}