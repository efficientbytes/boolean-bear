package app.efficientbytes.booleanbear.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.efficientbytes.booleanbear.database.dao.AdsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AdFreeSessionWorker(
    context: Context,
    workerParameters: WorkerParameters
) :
    CoroutineWorker(context, workerParameters), KoinComponent {

    private val adsDao: AdsDao by inject()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                adsDao.deleteActiveAdTemplate()
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }

    }

}