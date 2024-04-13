package app.efficientbytes.booleanbear.viewmodels

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.efficientbytes.booleanbear.models.ContentViewType
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PlayDetails
import app.efficientbytes.booleanbear.services.models.PlayUrl
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.YoutubeContentView
import app.efficientbytes.booleanbear.utils.ContentDetailsLiveListener
import app.efficientbytes.booleanbear.utils.InstructorLiveListener
import app.efficientbytes.booleanbear.utils.MentionedLinksLiveListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ShuffledContentPlayerViewModel(
    private val assetsRepository: AssetsRepository,
    private val externalScope: CoroutineScope,
    private val instructorLiveListener: InstructorLiveListener,
    private val mentionedLinksLiveListener: MentionedLinksLiveListener,
    private val contentDetailsLiveListener: ContentDetailsLiveListener
) : ViewModel(), LifecycleEventObserver, AssetsRepository.InstructorProfileListener,
    AssetsRepository.MentionedLinksListener {

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
        playDetailsJob = externalScope.launch {
            assetsRepository.getPlayDetails(contentId).collect {
                _playDetails.postValue(it)
                contentDetailsLiveListener.setContentDetailsStatus(it)
            }
        }
    }

    private val _suggestedContent: MutableLiveData<DataStatus<YoutubeContentView?>> =
        MutableLiveData()
    val suggestedContent: LiveData<DataStatus<YoutubeContentView?>> = _suggestedContent
    private var suggestedContentJob: Job? = null
    fun getSuggestedContent(contentId: String) {
        suggestedContentJob = externalScope.launch {
            assetsRepository.fetchContent(contentId, ContentViewType.YOUTUBE).collect {
                _suggestedContent.postValue(it)
            }
        }
    }

    private var instructorProfileJob: Job? = null
    fun getInstructorProfile(instructorId: String) {
        instructorProfileJob = externalScope.launch {
            assetsRepository.getInstructorDetails(instructorId, this@ShuffledContentPlayerViewModel)
        }
    }

    private var mentionedLinksJob: Job? = null
    fun getMentionedLinks(list: List<String>) {
        mentionedLinksJob = externalScope.launch {
            assetsRepository.getAllMentionedLinks(list, this@ShuffledContentPlayerViewModel)
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
                if (instructorProfileJob != null) {
                    instructorProfileJob?.cancel()
                }
                if (mentionedLinksJob != null) {
                    mentionedLinksJob?.cancel()
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

    override fun onInstructorProfileDataStatusChanged(status: DataStatus<RemoteInstructorProfile>) {
        instructorLiveListener.setInstructorStatus(status)
    }

    override fun onMentionedLinkDataStatusChanged(status: DataStatus<List<RemoteMentionedLink>>) {
        Log.i("VIEW-MODEL", "status changed. Status is ${status.isSuccessful}")
        mentionedLinksLiveListener.setMentionedLinksStatus(status)
    }

}