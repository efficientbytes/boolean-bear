package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.StatisticsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.ReelDetails
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.utils.ContentDetailsLiveListener
import app.efficientbytes.booleanbear.utils.InstructorLiveListener
import app.efficientbytes.booleanbear.utils.MentionedLinksLiveListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReelPlayerViewModel(
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope,
    private val instructorLiveListener: InstructorLiveListener,
    private val mentionedLinksLiveListener: MentionedLinksLiveListener,
    private val contentDetailsLiveListener: ContentDetailsLiveListener,
    private val statisticsRepository: StatisticsRepository
) : ViewModel(), LifecycleEventObserver, AssetsRepository.InstructorProfileListener,
    AssetsRepository.MentionedLinksListener {

    companion object {

        var countRecorded = false
    }

    private val _reelPlayLink: MutableLiveData<DataStatus<String>> = MutableLiveData()
    val reelPlayLink: LiveData<DataStatus<String>> = _reelPlayLink
    private var reelPlayLinkJob: Job? = null

    fun getReelPlayLink(reelId: String) {
        reelPlayLinkJob = externalScope.launch(Dispatchers.IO) {
            assetsRepository.getReelPlayLink(reelId).collect {
                _reelPlayLink.postValue(it)
            }
        }
    }

    private val _reelDetails: MutableLiveData<DataStatus<ReelDetails>> = MutableLiveData()
    val reelDetails: LiveData<DataStatus<ReelDetails>> = _reelDetails
    private var reelDetailsJob: Job? = null

    fun getReelDetails(reelId: String) {
        reelDetailsJob = externalScope.launch {
            assetsRepository.getReelDetails(reelId).collect {
                _reelDetails.postValue(it)
                contentDetailsLiveListener.setContentDetailsStatus(it)
            }
        }
    }

    private val _nextReel: MutableLiveData<DataStatus<RemoteReel>> =
        MutableLiveData()
    val nextReel: LiveData<DataStatus<RemoteReel>> = _nextReel
    private var nextReelJob: Job? = null
    fun getSuggestedContent(reelId: String) {
        nextReelJob = externalScope.launch {
            assetsRepository.getReel(reelId).collect {
                _nextReel.postValue(it)
            }
        }
    }

    private var instructorProfileJob: Job? = null
    fun getInstructorProfile(instructorId: String) {
        instructorProfileJob = externalScope.launch {
            assetsRepository.getInstructorDetails(instructorId, this@ReelPlayerViewModel)
        }
    }

    private var mentionedLinksJob: Job? = null
    fun getMentionedLinks(list: List<String>) {
        mentionedLinksJob = externalScope.launch {
            assetsRepository.getAllMentionedLinks(list, this@ReelPlayerViewModel)
        }
    }

    private val _viewCount: MutableLiveData<DataStatus<Unit>> = MutableLiveData()
    val viewCount: LiveData<DataStatus<Unit>> = _viewCount
    fun increaseContentViewCount(contentId: String) {
        externalScope.launch {
            statisticsRepository.increaseContentViewCount(contentId).collect {
                _viewCount.postValue(it)
            }
        }
    }

    fun addToWatchHistory(contentId: String) {
        /* viewModelScope.launch(Dispatchers.IO) {
             statisticsRepository.addReelToWatchHistory(contentId)
         }*/
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event.targetState) {
            Lifecycle.State.DESTROYED -> {
                if (reelPlayLinkJob != null) {
                    reelPlayLinkJob?.cancel()
                }
                if (reelDetailsJob != null) {
                    reelDetailsJob?.cancel()
                }
                if (nextReelJob != null) {
                    nextReelJob?.cancel()
                }
                if (instructorProfileJob != null) {
                    instructorProfileJob?.cancel()
                }
                if (mentionedLinksJob != null) {
                    mentionedLinksJob?.cancel()
                }
                countRecorded = false
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

    override fun onInstructorProfileDataStatusChanged(status: DataStatus<RemoteInstructorProfile>) {
        instructorLiveListener.setInstructorStatus(status)
    }

    override fun onMentionedLinkDataStatusChanged(status: DataStatus<List<RemoteMentionedLink>>) {
        mentionedLinksLiveListener.setMentionedLinksStatus(status)
    }

}