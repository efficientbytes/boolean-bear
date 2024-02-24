package app.efficientbytes.androidnow.repositories

import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.CoursesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CourseRepository(private val coursesService: CoursesService) {

    suspend fun pullShortCourses(contentType: String) = flow {
        emit(DataStatus.loading())
        val result = coursesService.getAllShortsCourses(contentType)
        when (result.code()) {
            200 -> {
                val courseList = result.body() ?: emptyList()
                emit(DataStatus.success(courseList))
            }

            400, 500 -> emit(DataStatus.failed(result.message().toString()))
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun pullShortCourse(courseID: String) = flow {
        emit(DataStatus.loading())
        val result = coursesService.getShortsCourse(courseID)
        when (result.code()) {
            200 -> {
                val course = result.body()
                emit(DataStatus.success(course))
            }

            400, 500 -> emit(DataStatus.failed(result.message().toString()))
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)


}