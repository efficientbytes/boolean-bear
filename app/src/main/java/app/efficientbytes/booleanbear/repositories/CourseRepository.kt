package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.CoursesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CourseRepository(private val coursesService: CoursesService) {

    suspend fun pullShortCourses(contentType: String) = flow {
        emit(DataStatus.loading())
        val response = coursesService.getAllShortsCourses(contentType)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val courseList = response.body() ?: emptyList()
                emit(DataStatus.success(courseList))
            }

            responseCode >= 400 -> {
                emit(DataStatus.failed(response.message().toString()))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun pullShortCourse(courseID: String) = flow {
        emit(DataStatus.loading())
        val response = coursesService.getShortsCourse(courseID)
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val course = response.body()
                emit(DataStatus.success(course))
            }

            responseCode >= 400 -> {
                emit(DataStatus.failed(response.message().toString()))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)


}