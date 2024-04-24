package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.database.models.ScreenTimingPerDay
import app.efficientbytes.booleanbear.services.models.RequestContentCount
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface StatisticsService {

    @FormUrlEncoded
    @POST("statistics/screen-timing/app/{userAccountId}/")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun uploadScreenTimings(
        @Path("userAccountId") userAccountId: String,
        @Field("screenTimingPerDayList") screenTimingPerDayList: List<ScreenTimingPerDay>
    ): Response<ResponseMessage>

    @FormUrlEncoded
    @POST("statistics/content-views/app/{contentId}/")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun increaseContentViewCount(
        @Path("contentId") contentId: String,
        @Field("userAccountId") userAccountId: String? = null,
        @Field("message") message: String? = null
    ): Response<RequestContentCount>

}