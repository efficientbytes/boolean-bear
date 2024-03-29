package app.efficientbytes.booleanbear.viewmodels

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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.database.models.ContentCategory
import app.efficientbytes.booleanbear.models.FeedShortsCourse
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.CourseRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseViewModel(
    private val courseRepository: CourseRepository,
    private val assetsRepository: AssetsRepository
) : ViewModel(),
    LifecycleEventObserver {

    private val tagCourseViewModel = "COURSE-VIEW-MODEL"
    private val _allShortCourses: MutableLiveData<DataStatus<List<FeedShortsCourse>>> =
        MutableLiveData()
    val allShortCourses: LiveData<DataStatus<List<FeedShortsCourse>>> = _allShortCourses

    suspend fun pullAllShortCourses(contentType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            courseRepository.pullShortCourses(contentType).collect {
                _allShortCourses.postValue(it)
            }
        }
    }

    val contentCategoriesFromDB: LiveData<MutableList<ContentCategory>> =
        assetsRepository.contentCategoriesFromDB.asLiveData()

    private fun getContentCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.getContentCategories().collect {
                when (it.status) {
                    DataStatus.Status.Failed -> {

                    }

                    DataStatus.Status.Loading -> {

                    }

                    DataStatus.Status.Success -> {
                        it.data?.let { list -> assetsRepository.saveContentCategories(list.categoryList) }
                    }
                }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getContentCategories()
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