package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.ResponseMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShareFeedbackViewModel :
    ViewModel(), KoinComponent {

    private val feedbackNSupportRepository: FeedbackNSupportRepository by inject()
    private val _feedbackUploadStatus: MutableLiveData<DataStatus<ResponseMessage?>> =
        MutableLiveData()
    val feedbackUploadStatus: LiveData<DataStatus<ResponseMessage?>> = _feedbackUploadStatus

    fun uploadFeedback(feedback: String) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackNSupportRepository.postFeedback(feedback)
                .collect {
                    _feedbackUploadStatus.postValue(it)
                }
        }
    }

}