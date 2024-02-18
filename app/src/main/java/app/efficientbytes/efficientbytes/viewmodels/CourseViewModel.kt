package app.efficientbytes.efficientbytes.viewmodels

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
import app.efficientbytes.efficientbytes.enums.COURSE_CONTENT_TYPE
import app.efficientbytes.efficientbytes.models.FeedShortsCourse
import app.efficientbytes.efficientbytes.repositories.CourseRepository
import app.efficientbytes.efficientbytes.repositories.models.DataStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseViewModel(private val courseRepository: CourseRepository) : ViewModel(),
    LifecycleEventObserver {

    private val _allShortCourses: MutableLiveData<DataStatus<List<FeedShortsCourse>>> =
        MutableLiveData()
    val allShortCourses: LiveData<DataStatus<List<FeedShortsCourse>>> = _allShortCourses

    fun pullAllShortCourses(contentType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            courseRepository.pullShortCourses(contentType).collect {
                Log.i("ViewModel","List is : ${it.data.toString()}")
                _allShortCourses.postValue(it)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {

            }

            ON_START -> {
                pullAllShortCourses(COURSE_CONTENT_TYPE.CONCEPT_LEARNING.getContentType())
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