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
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope
) : ViewModel(),
    LifecycleEventObserver {

    private val _reelTopics: MutableLiveData<DataStatus<List<RemoteReelTopic>>> = MutableLiveData()
    val reelTopics: LiveData<DataStatus<List<RemoteReelTopic>>> = _reelTopics

    fun getReelTopics() {
        externalScope.launch {
            assetsRepository.getReelTopics().collect {
                when (it.status) {
                    DataStatus.Status.Success, DataStatus.Status.Loading -> {
                        _reelTopics.postValue(it)
                    }

                    else -> {
                        delay(4000)
                        _reelTopics.postValue(it)
                    }
                }
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