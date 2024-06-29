package app.efficientbytes.booleanbear.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
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

class PauseRewardedAdForegroundService : LifecycleService() {

    companion object {

        const val ACTION_STOP_SERVICE = "stop_countdown_service"
        const val EXTRA_PAUSE_DURATION = "extra_pause_duration"
        const val EXTRA_CONCLUSION_MESSAGE = "extra_conclusion_message"
    }

    private var pauseDurationInMillis = 0L
    private var endMessage = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopService()
            return START_NOT_STICKY
        }
        val pauseDurationInMinutes = intent?.getLongExtra(EXTRA_PAUSE_DURATION, 0L)
        endMessage = intent?.getStringExtra(EXTRA_CONCLUSION_MESSAGE)
            ?: "Your ad-free period has concluded. We appreciate your support!"

        if (pauseDurationInMinutes == null || pauseDurationInMinutes <= 0) {
            stopService()
            return START_NOT_STICKY
        }

        pauseDurationInMillis = TimeUnit.MINUTES.toMillis(pauseDurationInMinutes)

        startForeground(123, showCountdownNotification().build())

        lifecycleScope.launch(Dispatchers.IO) {
            var timeLeft = pauseDurationInMillis
            while (timeLeft > 0) {
                updateCountdownNotification(timeLeft)
                delay(1000)
                timeLeft -= 1000
            }
            showConclusionNotification()
            stopService()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    private fun showCountdownNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            this,
            NotificationsHelper.channelId(this, getString(R.string.ad_free_content_countdown))
        )
            .setContentTitle("Ad-Free Content Countdown")
            .setContentText("Time left: ${pauseDurationInMillis / 1000 / 60} minutes")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(pauseDurationInMillis.toInt(), pauseDurationInMillis.toInt(), false)

    }

    private fun updateCountdownNotification(timeLeftMillis: Long) {
        val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis)
        val notification = showCountdownNotification()
            .setContentText("Ad-free content: $minutesLeft minutes remaining")
            .setProgress(pauseDurationInMillis.toInt(), timeLeftMillis.toInt(), false)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(123, notification)
    }

    private fun showConclusionNotification() {
        val notification = NotificationCompat.Builder(
            this,
            NotificationsHelper.channelId(this, getString(R.string.ad_free_content_conclusion))
        )
            .setContentTitle("Ad-Free Session Has Concluded")
            .setContentText(endMessage)
            .setSmallIcon(R.mipmap.ic_launcher_round)
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
        NotificationManagerCompat.from(this).notify(124, notification)
    }


}