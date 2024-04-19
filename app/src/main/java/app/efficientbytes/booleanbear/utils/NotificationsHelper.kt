package app.efficientbytes.booleanbear.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.ui.activities.MainActivity

object NotificationsHelper {

    fun createNotificationChannel(
        context: Context,
        name: String,
        description: String,
        showBadge: Boolean = false,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        enableLights: Boolean = false,
        enableVibration: Boolean = false,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val sound =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + "app.efficientbytes.booleanbear" + "/" + app.efficientbytes.booleanbear.R.raw.level_up_191997_sound)
            val channelId = channelId(context, name)
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)
            channel.enableLights(enableLights)
            channel.enableVibration(enableVibration)
            channel.setSound((sound), audioAttributes)
            if (enableLights) channel.vibrationPattern =
                longArrayOf(
                    200,
                    400,
                    600,
                    800,
                    1000,
                    200,
                    400,
                    600,
                    800,
                    1000,
                    200,
                    400,
                    600,
                    800,
                    1000
                )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotifications(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        bigTextStyle: Boolean,
        pendingIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
        val defaultIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val defaultPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            defaultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val sound =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + "app.efficientbytes.booleanbear" + "/" + app.efficientbytes.booleanbear.R.raw.level_up_191997_sound)
        val builder = when (bigTextStyle) {
            true -> {
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(sound)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(body)
                    )

            }

            false -> {
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(sound)

            }
        }
        return if (pendingIntent == null) builder.setContentIntent(defaultPendingIntent) else builder.setContentIntent(
            pendingIntent
        )
    }

    fun channelId(context: Context, channelName: String): String =
        "${context.packageName}-$channelName"

    fun getManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}