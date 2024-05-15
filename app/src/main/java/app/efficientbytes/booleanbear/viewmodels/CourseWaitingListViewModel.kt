package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import kotlinx.coroutines.launch

class CourseWaitingListViewModel(private val assetsRepository: AssetsRepository) :
    ViewModel(), AssetsRepository.JoinCourseWaitingListListener {

    private val _courseWaitingList: MutableLiveData<DataStatus<Boolean>?> = MutableLiveData()
    val courseWaitingList: LiveData<DataStatus<Boolean>?> = _courseWaitingList

    fun joinCourseWaitingList(courseId: String) {
        assetsRepository.joinCourseWaitingList(courseId, this@CourseWaitingListViewModel)
    }

    override fun onJoinCourseWaitingListDataStatusChanged(status: DataStatus<Boolean>) {
        viewModelScope.launch {
            _courseWaitingList.postValue(status)
        }
    }

    fun resetWaitingList(){
        viewModelScope.launch {
            _courseWaitingList.postValue(null)
        }
    }

    fun hasUserJoinedWaitList(courseId: String): Boolean =
        assetsRepository.userJoinedWaitingList(courseId)

}