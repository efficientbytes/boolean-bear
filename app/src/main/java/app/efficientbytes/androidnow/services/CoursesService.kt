package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.enums.COURSE_CONTENT_TYPE
import app.efficientbytes.androidnow.models.FeedShortsCourse
import app.efficientbytes.androidnow.models.WatchShortsCourse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoursesService {

    @GET("byte-courses/shorts")
    suspend fun getAllShortsCourses(
        @Query("contentType") contentType: String=COURSE_CONTENT_TYPE.CONCEPT_LEARNING.getContentType()
    ): Response<List<FeedShortsCourse>>

    @GET("byte-courses/shorts/{courseId}")
    suspend fun getShortsCourse(
        @Path("courseId") courseId: String
    ): Response<WatchShortsCourse>

    @GET("byte-courses/series")
    suspend fun getAllSeriesCourses(
        @Query("contentType") contentType: String
    ): Response<List<FeedShortsCourse>>

}