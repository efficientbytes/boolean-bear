package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.ResponseMessage
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface StatisticsService {

    @FormUrlEncoded
    @POST("statistics/content-views/app/{contentId}/")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun increaseContentViewCount(
        @Path("contentId") contentId: String,
        @Field("message") message: String? = null
    ): Response<ResponseMessage>
    /* @FormUrlEncoded
     @POST("statistics/reels/watch-history/")
     @Headers("Content-Type: application/x-www-form-urlencoded")
     suspend fun addReelToWatchHistory(
         @Field("contentId") contentId: String,
         @Field("startOfDayTimestamp") startOfDayTimestamp: Long,
         @Field("timestamp") timestamp: Long,
     ): Response<ResponseMessage>*/

}