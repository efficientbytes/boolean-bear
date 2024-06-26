package app.efficientbytes.booleanbear.repositories

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import app.efficientbytes.booleanbear.database.dao.StatisticsDao
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.StatisticsService
import app.efficientbytes.booleanbear.utils.NoInternetException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import java.io.IOException
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.Date
import java.util.Locale

class StatisticsRepository(
    private val externalScope: CoroutineScope,
    private val statisticsDao: StatisticsDao,
    private val statisticsService: StatisticsService
) {

    private suspend fun getServerTime(): Long? {
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

    fun increaseContentViewCount(contentId: String) = flow {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            emit(DataStatus.loading<Unit>())
            try {
                val response =
                    statisticsService.increaseContentViewCount(
                        contentId
                    )
                val responseCode = response.code()
                if (responseCode == 200) emit(DataStatus.success(Unit))
            } catch (noInternet: NoInternetException) {
                emit(DataStatus.noInternet<Unit>())
            } catch (socketTimeOutException: SocketTimeoutException) {
                emit(DataStatus.noInternet<Unit>())
            } catch (exception: IOException) {
                emit(DataStatus.unknownException<Unit>(exception.message.toString()))
            }
        }
    }.catch { t -> emit(DataStatus.unknownException<Unit>(t.message.toString())) }
        .flowOn(Dispatchers.IO)

}