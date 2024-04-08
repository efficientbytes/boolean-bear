package app.efficientbytes.booleanbear.repositories

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import app.efficientbytes.booleanbear.database.dao.StatisticsDao
import app.efficientbytes.booleanbear.database.models.ScreenTiming
import app.efficientbytes.booleanbear.services.StatisticsService
import app.efficientbytes.booleanbear.utils.getTodayDateComponent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import java.net.InetAddress
import java.util.Date
import java.util.Locale

class StatisticsRepository(
    private val externalScope: CoroutineScope,
    private val statisticsDao: StatisticsDao,
    private val statisticsService: StatisticsService
) {

    fun noteDownScreenOpeningTime() {
        if (FirebaseAuth.getInstance().currentUser == null) return
        externalScope.launch {
            val currentDate = getServerTime()
            currentDate?.let {
                val today = getTodayDateComponent(it)
                statisticsDao.noteDownScreenOpeningTime(
                    ScreenTiming(
                        date = today.time,
                        opened = it
                    )
                )
            }
        }
    }

    fun noteDownScreenClosingTime() {
        if (FirebaseAuth.getInstance().currentUser == null) return
        externalScope.launch {
            val currentDate = getServerTime()
            currentDate?.let {
                val today = getTodayDateComponent(it)
                statisticsDao.noteDownScreenClosingTime(today.time, it)
            }
        }
    }

    fun uploadPendingScreenTiming() {
        if (FirebaseAuth.getInstance().currentUser == null) return
        externalScope.launch {
            val isEmpty = statisticsDao.screenTimingIsEmpty().isEmpty()
            if (!isEmpty) {
                val currentDate = getServerTime() ?: return@launch
                val list =
                    statisticsDao.getTotalScreenTimePerDayBasisForAllDayExceptFor(currentDate)
                val response = statisticsService.uploadScreenTimings(list)
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        statisticsDao.deleteScreenTimingForAllDayExceptFor(currentDate)
                    }
                }
            }
        }
    }

    fun deleteUserScreenTime() {
        externalScope.launch {
            statisticsDao.deleteScreenTimeForAllDay()
        }
    }

    suspend fun getServerTime(): Long? {
        val result = externalScope.async {
            val timeServer = "time.google.com"
            val client = NTPUDPClient()
            client.defaultTimeout = 10_000
            var time: String? = null
            try {
                val inetAddress = InetAddress.getByName(timeServer)
                val timeInfo: TimeInfo = client.getTime(inetAddress)
                time = timeInfo.message.receiveTimeStamp.toUTCString()
            } catch (e: Exception) {
                time = null
            } finally {
                client.close()
            }
            val utcMilliseconds = time?.let {
                val dateFormat =
                    SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss.SSS 'UTC'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date: Date = dateFormat.parse(it)
                date.time
            }

            return@async utcMilliseconds
        }
        return result.await()
    }

}