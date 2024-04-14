package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.database.models.ScreenTimingPerDay
import app.efficientbytes.booleanbear.services.models.RequestContentCount
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface StatisticsService {

    @POST("statistics/screen-timing/app/{userAccountId}/")
    suspend fun uploadScreenTimings(
        @Path("userAccountId") userAccountId: String,
        @Body screenTimingPerDayList: List<ScreenTimingPerDay>
    ): Response<ResponseMessage>

    @POST("statistics/content-views/app/{contentId}/")
    suspend fun increaseContentViewCount(
        @Path("contentId") contentId: String,
        @Body requestContentCount: RequestContentCount
    ): Response<RequestContentCount>

}