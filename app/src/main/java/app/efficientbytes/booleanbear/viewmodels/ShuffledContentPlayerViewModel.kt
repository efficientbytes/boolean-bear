package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PlayDetails
import app.efficientbytes.booleanbear.services.models.PlayUrl
import app.efficientbytes.booleanbear.services.models.YoutubeContentView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ShuffledContentPlayerViewModel(
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope,
) : ViewModel(), LifecycleEventObserver {

    private val _playUrl: MutableLiveData<DataStatus<PlayUrl?>> = MutableLiveData()
    val playUrl: LiveData<DataStatus<PlayUrl?>> = _playUrl
    private var playUrlJob: Job? = null

    fun getPlayUrl(contentId: String) {
        playUrlJob = externalScope.launch(Dispatchers.IO) {
            assetsRepository.getPlayUrl(contentId).collect {
                _playUrl.postValue(it)
            }
        }
    }

    private val _playDetails: MutableLiveData<DataStatus<PlayDetails?>> = MutableLiveData()
    val playDetails: LiveData<DataStatus<PlayDetails?>> = _playDetails
    private var playDetailsJob: Job? = null

    fun getPlayDetails(contentId: String) {
        playDetailsJob = externalScope.launch(Dispatchers.IO) {
            assetsRepository.getPlayDetails(contentId).collect {
                _playDetails.postValue(it)
            }
        }
    }

    private val _suggestedContent: MutableLiveData<DataStatus<YoutubeContentView?>> =
        MutableLiveData()
    val suggestedContent: LiveData<DataStatus<YoutubeContentView?>> = _suggestedContent
    private var suggestedContentJob: Job? = null
    fun getSuggestedContent(contentId: String) {
        suggestedContentJob = externalScope.launch(Dispatchers.IO) {
            assetsRepository.getYoutubeTypeContentViewForContentId(contentId).collect {
                _suggestedContent.postValue(it)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event.targetState) {
            Lifecycle.State.DESTROYED -> {
                if (playUrlJob != null) {
                    playUrlJob?.cancel()
                }
                if (playDetailsJob != null) {
                    playDetailsJob?.cancel()
                }
                if (suggestedContentJob != null) {
                    suggestedContentJob?.cancel()
                }
            }

            Lifecycle.State.INITIALIZED -> {

            }

            Lifecycle.State.CREATED -> {

            }

            Lifecycle.State.STARTED -> {

            }

            Lifecycle.State.RESUMED -> {

            }
        }
    }


}