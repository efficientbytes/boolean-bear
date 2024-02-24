package app.efficientbytes.androidnow.viewmodels

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.androidnow.enums.COURSE_CONTENT_TYPE
import app.efficientbytes.androidnow.models.FeedShortsCourse
import app.efficientbytes.androidnow.repositories.CourseRepository
import app.efficientbytes.androidnow.repositories.models.DataStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class CourseViewModel(private val courseRepository: CourseRepository) : ViewModel(),
    LifecycleEventObserver {

    private val _allShortCourses: MutableLiveData<DataStatus<List<FeedShortsCourse>>> =
        MutableLiveData()
    val allShortCourses: LiveData<DataStatus<List<FeedShortsCourse>>> = _allShortCourses

    private var allCoursesJob : Job? = null

    suspend fun pullAllShortCourses(contentType: String) {
        if (allCoursesJob?.isActive==true){
            allCoursesJob?.cancelAndJoin()
        }
        allCoursesJob = viewModelScope.launch(Dispatchers.IO) {
            courseRepository.pullShortCourses(contentType).collect {
                Log.i("ViewModel","List is : ${it.data.toString()}")
                _allShortCourses.postValue(it)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                allCoursesJob = viewModelScope.launch {
                    pullAllShortCourses(COURSE_CONTENT_TYPE.CONCEPT_LEARNING.getContentType())
                }
            }

            ON_START -> {

            }

            ON_RESUME -> {

            }

            ON_PAUSE -> {

            }

            ON_STOP -> {

            }

            ON_DESTROY -> {

            }

            ON_ANY -> {

            }
        }
    }

}