package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ListReelViewModel(
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope
) : ViewModel() {

    private val _reels: MutableLiveData<DataStatus<List<RemoteReel>>> = MutableLiveData()
    val reels: LiveData<DataStatus<List<RemoteReel>>> = _reels

    fun getReels(topicId: String) {
        externalScope.launch {
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

    private val _topicResult: MutableLiveData<DataStatus<RemoteReelTopic>> =
        MutableLiveData()
    val topicResult: LiveData<DataStatus<RemoteReelTopic>> = _topicResult

    fun getTopicDetail(topicId: String) {
        viewModelScope.launch {
            if (topicId.isEmpty()) {
                _topicResult.postValue(DataStatus.emptyResult())
            } else {
                assetsRepository.getReelTopicDetails(topicId).collect {
                    _topicResult.postValue(it)
                }
            }
        }
    }

}