package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.database.models.ScreenTimingPerDay
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StatisticsService {

    @POST("statistics/screen-timing/app/a8f676ef-edbe-400f-b12f-42d91cd8d5f3/")
    suspend fun uploadScreenTimings(@Body screenTimingPerDayList: List<ScreenTimingPerDay>): Response<ResponseMessage>

}