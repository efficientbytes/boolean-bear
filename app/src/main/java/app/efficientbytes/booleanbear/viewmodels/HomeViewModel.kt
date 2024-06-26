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
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope
) : ViewModel(),
    LifecycleEventObserver {

    private val _reelTopics: MutableLiveData<DataStatus<List<RemoteReelTopic>>> = MutableLiveData()
    val reelTopics: LiveData<DataStatus<List<RemoteReelTopic>>> = _reelTopics

    fun getReelTopics() {
        externalScope.launch {
            assetsRepository.getReelTopics().collect {
                _reelTopics.postValue(it)
            }
        }
    }

    private val _reels: MutableLiveData<DataStatus<List<RemoteReel>>> = MutableLiveData()
    val reels: LiveData<DataStatus<List<RemoteReel>>> = _reels
    private var fetchReelsJob: Job? = null

    fun getReels(topicId: String) {
        if (fetchReelsJob != null) {
            fetchReelsJob!!.cancel()
        }
        fetchReelsJob = externalScope.launch {
            assetsRepository.getReels(topicId).collect {
                _reels.postValue(it)
            }
        }
    }

    private val _searchResult: MutableLiveData<DataStatus<List<RemoteReel>>> =
        MutableLiveData()
    val searchResult: LiveData<DataStatus<List<RemoteReel>>> = _searchResult

    fun getReelQueries(topicId: String, query: String = "") {
        viewModelScope.launch {
            if (topicId.isEmpty() || topicId.isBlank()) {
                _searchResult.postValue(DataStatus.emptyResult())
            } else {
                val result = assetsRepository.getReelQueries(topicId, query)
                if (result.isNullOrEmpty()) _searchResult.postValue(DataStatus.emptyResult()) else _searchResult.postValue(
                    DataStatus.success(result)
                )
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getReelTopics()
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