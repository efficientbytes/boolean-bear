package app.efficientbytes.booleanbear.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.Feedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShareFeedbackViewModel(private val feedbackNSupportRepository: FeedbackNSupportRepository) :
    ViewModel() {

    private val _feedbackUploadStatus: MutableLiveData<DataStatus<Feedback?>> =
        MutableLiveData()
    val feedbackUploadStatus: LiveData<DataStatus<Feedback?>> = _feedbackUploadStatus

    fun uploadFeedback(feedback: String, userAccountId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackNSupportRepository.postFeedback(Feedback(feedback, userAccountId, ""))
                .collect {
                    _feedbackUploadStatus.postValue(it)
                }
        }
    }

}