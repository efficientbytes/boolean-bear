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
import app.efficientbytes.booleanbear.services.models.RemoteCourseBundle
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DiscoverViewModel : ViewModel(), KoinComponent,
    LifecycleEventObserver {

    private val assetsRepository: AssetsRepository by inject()
    private val externalScope: CoroutineScope by inject()
    private val _reelTopics: MutableLiveData<DataStatus<List<RemoteReelTopic>>> = MutableLiveData()
    val reelTopics: LiveData<DataStatus<List<RemoteReelTopic>>> = _reelTopics

    fun getReelTopics() {
        externalScope.launch {
            assetsRepository.getReelTopics().collect {
                when (it.status) {
                    DataStatus.Status.Loading -> {
                        _reelTopics.postValue(it)
                    }

                    DataStatus.Status.Success -> {
                        _reelTopics.postValue(it)
                    }

                    else -> {
                        _reelTopics.postValue(it)
                    }
                }
            }
        }
    }

    private val _courseBundle: MutableLiveData<DataStatus<List<RemoteCourseBundle>>> =
        MutableLiveData()
    val courseBundle: LiveData<DataStatus<List<RemoteCourseBundle>>> = _courseBundle

    fun getCourseBundle() {
        externalScope.launch {
            assetsRepository.getCourseBundle().collect {
                when (it.status) {
                    DataStatus.Status.Loading -> {
                        _courseBundle.postValue(it)
                    }

                    DataStatus.Status.Success -> {
                        _courseBundle.postValue(it)
                    }

                    else -> {
                        _courseBundle.postValue(it)
                    }
                }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getReelTopics()
                getCourseBundle()
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