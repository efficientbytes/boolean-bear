package app.efficientbytes.booleanbear.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentShuffledContentPlayerBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.viewmodels.ShuffledContentPlayerViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import org.koin.android.ext.android.inject
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.net.UnknownHostException

class ShuffledContentPlayerFragment : Fragment(), AnimationListener {

    private lateinit var _binding: FragmentShuffledContentPlayerBinding
    private val binding get() = _binding

    //exo player
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private lateinit var trackSelector: DefaultTrackSelector
    private var mediaItem: MediaItem? = null

    //all view late init
    private lateinit var rootView: View
    private lateinit var playerTitleText: MaterialTextView
    private lateinit var playerInstructorNameText: MaterialTextView
    private lateinit var playerQualityMenu: LinearLayout
    private lateinit var fullScreenButton: ImageButton
    private lateinit var gifDrawable: GifDrawable
    private lateinit var contentId: String

    //flags
    private var isFullScreen = false
    private var isPlayingSuggested = false
    private val viewModel: ShuffledContentPlayerViewModel by inject()
    private var nextSuggestedContentId: String? = null
    private var noInternet = false
    private val connectivityListener: ConnectivityListener by inject()
    private var shuffledContentDescriptionFragment: ShuffledContentDescriptionFragment? = null
    private var contentTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = ShuffledContentPlayerFragmentArgs.fromBundle(bundle)
        val contentId = args.contentId
        this.contentId = contentId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShuffledContentPlayerBinding.inflate(inflater, container, false)
        rootView = binding.root
        lifecycle.addObserver(viewModel)
        return rootView
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gifDrawable = binding.booleanBearLoadingGif.drawable as GifDrawable
        gifDrawable.addAnimationListener(this@ShuffledContentPlayerFragment)
        playerTitleText = rootView.findViewById(R.id.playerTitleValueTextView)
        playerInstructorNameText = rootView.findViewById(R.id.playerInstructorNameValueTextView)
        playerQualityMenu = rootView.findViewById(R.id.playerQualityMenuLinearLayout)
        val closeButton = rootView.findViewById<ImageButton>(R.id.playerCancelAndGoBackImageButton)
        val playerQualityButton = rootView.findViewById<ImageButton>(R.id.exo_track_selection_view)
        fullScreenButton = rootView.findViewById(R.id.playerFullScreenImageButton)

        if (!connectivityListener.isInternetAvailable()) {
            binding.parentConstraintLayout.visibility = View.GONE
            binding.noInternetLinearLayout.visibility = View.VISIBLE
        } else {
            binding.noInternetLinearLayout.visibility = View.GONE
            binding.parentConstraintLayout.visibility = View.VISIBLE
            viewModel.getPlayUrl(contentId)
            viewModel.getPlayDetails(contentId)
        }

        viewModel.playUrl.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {

                }

                DataStatus.Status.Loading -> {
                    binding.noNetworkLinearLayout.visibility = View.GONE
                    binding.booleanBearLoadingGif.visibility = View.VISIBLE
                    gifDrawable.start()
                }

                DataStatus.Status.Success -> {
                    it.data?.let { playUrl ->
                        mediaItem = playUrl.playUrl?.let { url -> MediaItem.fromUri(url) }
                        initializePlayer()
                    }
                }

                DataStatus.Status.NoInternet -> {
                    noInternet = true
                    gifDrawable.stop()
                    binding.booleanBearLoadingGif.visibility = View.GONE
                    binding.noNetworkLinearLayout.visibility = View.VISIBLE
                }

                DataStatus.Status.TimeOut -> {

                }

                DataStatus.Status.EmptyResult -> {
                    val snackBar = Snackbar.make(
                        binding.constraintLayout,
                        "Requested content is not available",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.show()
                    findNavController().popBackStack()
                }

                else -> {

                }
            }
        }

        viewModel.playDetails.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.contentDetailsConstraintLayout.visibility = View.GONE
                    binding.suggestedContentCardView.visibility = View.GONE

                    binding.shimmerContentDetails.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE

                    binding.shimmerContentDetails.stopShimmer()
                    binding.shimmerSuggestedContent.stopShimmer()
                }

                DataStatus.Status.Loading -> {
                    binding.contentDetailsConstraintLayout.visibility = View.GONE
                    binding.suggestedContentCardView.visibility = View.GONE

                    binding.shimmerContentDetails.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE

                    binding.shimmerContentDetails.startShimmer()
                    binding.shimmerSuggestedContent.startShimmer()
                }

                DataStatus.Status.Success -> {
                    binding.shimmerContentDetails.visibility = View.GONE
                    binding.contentDetailsConstraintLayout.visibility = View.VISIBLE
                    binding.shimmerContentDetails.stopShimmer()

                    it.data?.let { playDetails ->
                        playDetails.nextSuggestion?.let { suggestedContentId ->
                            nextSuggestedContentId = suggestedContentId
                            viewModel.getSuggestedContent(suggestedContentId)
                        }
                        viewModel.getInstructorProfile(playDetails.instructorId)
                        playDetails.mentionedLinkIds?.let { mentionedLinkIds ->
                            viewModel.getMentionedLinks(
                                mentionedLinkIds
                            )
                        }
                        if (playDetails.nextSuggestion == null) {
                            binding.shimmerSuggestedContent.stopShimmer()
                            binding.shimmerSuggestedContent.visibility = View.GONE
                            binding.suggestedContentCardView.visibility = View.GONE
                            nextSuggestedContentId = null
                        }
                        binding.playDetails = playDetails
                        playerTitleText.text = playDetails.title
                        contentTitle = playDetails.title
                        val instructorFullName = if (playDetails.instructorLastName == null) {
                            playDetails.instructorFirstName
                        } else {
                            playDetails.instructorFirstName + " " + playDetails.instructorLastName
                        }
                        playerInstructorNameText.text = instructorFullName
                    }
                }

                DataStatus.Status.NoInternet -> {
                    noInternet = true
                    binding.contentDetailsConstraintLayout.visibility = View.GONE
                    binding.suggestedContentCardView.visibility = View.GONE

                    binding.shimmerContentDetails.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE

                    binding.shimmerContentDetails.stopShimmer()
                    binding.shimmerSuggestedContent.stopShimmer()
                }

                DataStatus.Status.TimeOut -> {
                    binding.contentDetailsConstraintLayout.visibility = View.GONE
                    binding.suggestedContentCardView.visibility = View.GONE

                    binding.shimmerContentDetails.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE

                    binding.shimmerContentDetails.stopShimmer()
                    binding.shimmerSuggestedContent.stopShimmer()
                }

                DataStatus.Status.EmptyResult -> {
                    findNavController().popBackStack()
                }

                else -> {

                }
            }
        }

        viewModel.suggestedContent.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.shimmerSuggestedContent.stopShimmer()
                    binding.shimmerSuggestedContent.visibility = View.GONE
                    binding.suggestedContentCardView.visibility = View.GONE
                }

                DataStatus.Status.Loading -> {
                    binding.suggestedContentCardView.visibility = View.GONE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.startShimmer()
                }

                DataStatus.Status.Success -> {
                    val suggestedContent = it.data
                    suggestedContent?.let { youtubeContentView ->
                        binding.shimmerSuggestedContent.stopShimmer()
                        binding.shimmerSuggestedContent.visibility = View.GONE
                        binding.suggestedContentCardView.visibility = View.VISIBLE
                        binding.suggestedContentDetails = youtubeContentView
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.suggestedContentCardView.visibility = View.GONE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.stopShimmer()
                }

                DataStatus.Status.TimeOut -> {
                    binding.suggestedContentCardView.visibility = View.GONE
                    binding.shimmerSuggestedContent.visibility = View.VISIBLE
                    binding.shimmerSuggestedContent.stopShimmer()
                }

                else -> {

                }
            }
        }

        fullScreenButton.setOnClickListener {
            if (isFullScreen) {  // tapped on minimize button
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                fullScreenButton.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.full_screen_player_icon
                    )
                )
                isFullScreen = false
            } else { // tapped on full screen
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                isFullScreen = true
                fullScreenButton.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.portrait_player_icon
                    )
                )
            }
        }

        closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (isFullScreen) {
            playerTitleText.visibility = View.VISIBLE
            playerInstructorNameText.visibility = View.VISIBLE
        } else {
            playerTitleText.visibility = View.GONE
            playerInstructorNameText.visibility = View.GONE
        }

        playerQualityButton.setOnClickListener {
        }

        binding.retryButton.setOnClickListener {
            if (noInternet) {
                noInternet = false
                viewModel.getPlayUrl(contentId)
                viewModel.getPlayDetails(contentId)
            } else {
                initializePlayer()
            }
        }

        binding.suggestedContentCardView.setOnClickListener {
            nextSuggestedContentId?.let { contentId ->
                clearStartPosition()
                isPlayingSuggested = true
                this@ShuffledContentPlayerFragment.contentId = contentId
                ShuffledContentPlayerViewModel.countRecorded = false
                binding.videoPlayer.hideController()
                playerQualityMenu.visibility = View.GONE
                viewModel.getPlayUrl(contentId)
                viewModel.getPlayDetails(contentId)
            }
        }

        binding.retryAfterNoInternetButton.setOnClickListener {
            if (connectivityListener.isInternetAvailable()) {
                noInternet = false
                binding.noInternetLinearLayout.visibility = View.GONE
                binding.parentConstraintLayout.visibility = View.VISIBLE
                viewModel.getPlayUrl(contentId)
                viewModel.getPlayDetails(contentId)
            }
        }

        binding.descriptionLinearLayout.setOnClickListener {
            openDescriptionFragment()
        }

        binding.fullDescriptionLabelTextView.setOnClickListener {
            openDescriptionFragment()
        }
        viewModel.viewCount.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.NoInternet -> {
                    ShuffledContentPlayerViewModel.countRecorded = false
                }

                DataStatus.Status.TimeOut -> {
                    ShuffledContentPlayerViewModel.countRecorded = false
                }

                DataStatus.Status.Success -> {
                    ShuffledContentPlayerViewModel.countRecorded = true
                }

                else -> {}
            }
        }
        binding.shareContentLabelTextView.setOnClickListener {
            val shareLink = "https://app.booleanbear.com/watch/v/$contentId"
            val title = this@ShuffledContentPlayerFragment.contentTitle
            shareContent(shareLink, title)
        }
    }

    private fun openDescriptionFragment() {
        if (shuffledContentDescriptionFragment == null) {
            shuffledContentDescriptionFragment = ShuffledContentDescriptionFragment()
        }
        if (!ShuffledContentDescriptionFragment.isOpened) {
            ShuffledContentDescriptionFragment.isOpened = true
            shuffledContentDescriptionFragment!!.show(
                parentFragmentManager,
                ShuffledContentDescriptionFragment.SHUFFLED_DESCRIPTION_FRAGMENT
            )
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        trackSelector = DefaultTrackSelector(requireContext())
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setAllowVideoMixedMimeTypeAdaptiveness(true)
        )
        if (player == null) {
            val loadControl = DefaultLoadControl()
            player = ExoPlayer.Builder(requireContext())
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build()
                .also { exoPlayer ->
                    exoPlayer.playWhenReady = if (isPlayingSuggested) {
                        isPlayingSuggested = false
                        true
                    } else {
                        playWhenReady
                    }
                    exoPlayer.addListener(playbackStateListener)
                    binding.videoPlayer.player = exoPlayer
                    binding.videoPlayer.keepScreenOn = true
                }
        }
        if (isPlayingSuggested) {
            isPlayingSuggested = false
            player!!.playWhenReady = true
        }
        val haveStartPosition = mediaItemIndex != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(mediaItemIndex, playbackPosition)
        }
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        mediaItem?.let {
            HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(
                it
            )
        }?.also {
            player!!.setMediaSource(it,  /* resetPosition= */!haveStartPosition)
            player!!.prepare()
        }
        return
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            updateStartPosition()
            exoPlayer.release()
            exoPlayer.removeListener(playbackStateListener)
            binding.videoPlayer.player = null
        }
        player = null
    }

    private fun updateStartPosition() {
        player?.let { exoPlayer ->
            playWhenReady = exoPlayer.playWhenReady
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playbackPosition = 0.coerceAtLeast(exoPlayer.contentPosition.toInt()).toLong()
        }
    }

    private fun clearStartPosition() {
        playWhenReady = true
        mediaItemIndex = C.INDEX_UNSET
        playbackPosition = C.TIME_UNSET
    }

    private fun playbackStateListener() = object : Player.Listener {
        @OptIn(UnstableApi::class)
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    binding.videoPlayer.hideController()
                    playerQualityMenu.visibility = View.GONE
                    gifDrawable.stop()
                    binding.booleanBearLoadingGif.visibility = View.GONE
                }

                ExoPlayer.STATE_BUFFERING -> {
                    binding.videoPlayer.hideController()
                    binding.noNetworkLinearLayout.visibility = View.GONE
                    playerQualityMenu.visibility = View.GONE
                    binding.booleanBearLoadingGif.visibility = View.VISIBLE
                    gifDrawable.start()
                }

                ExoPlayer.STATE_READY -> {
                    if (!ShuffledContentPlayerViewModel.countRecorded) {
                        ShuffledContentPlayerViewModel.countRecorded = true
                        viewModel.increaseContentViewCount(this@ShuffledContentPlayerFragment.contentId)
                    }
                    binding.videoPlayer.showController()
                    binding.noNetworkLinearLayout.visibility = View.GONE
                    playerQualityMenu.visibility = View.VISIBLE
                    gifDrawable.stop()
                    binding.booleanBearLoadingGif.visibility = View.GONE
                }

                ExoPlayer.STATE_ENDED -> {
                    binding.videoPlayer.showController()
                    playerQualityMenu.visibility = View.GONE
                    nextSuggestedContentId?.let { contentId ->
                        binding.videoPlayer.hideController()
                        clearStartPosition()
                        isPlayingSuggested = true
                        this@ShuffledContentPlayerFragment.contentId = contentId
                        ShuffledContentPlayerViewModel.countRecorded = false
                        viewModel.getPlayUrl(contentId)
                        viewModel.getPlayDetails(contentId)
                    }
                    gifDrawable.stop()
                    binding.booleanBearLoadingGif.visibility = View.GONE
                }

                else -> {
                    playerQualityMenu.visibility = View.GONE
                    gifDrawable.stop()
                    binding.booleanBearLoadingGif.visibility = View.GONE
                }
            }
        }

        @OptIn(UnstableApi::class)
        override fun onPlayerError(error: PlaybackException) {
            val cause = error.cause
            updateStartPosition()
            if (cause is HttpDataSource.HttpDataSourceException) {
                if (cause is HttpDataSource.InvalidResponseCodeException) {
                    if (cause.responseCode == 403) {
                        viewModel.getPlayUrl(contentId)
                    }

                } else {
                    if (cause.cause is UnknownHostException) {
                        gifDrawable.stop()
                        binding.booleanBearLoadingGif.visibility = View.GONE
                        binding.noNetworkLinearLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Util.SDK_INT > 23) {
            initializePlayer()
            binding.videoPlayer.onResume()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
            binding.videoPlayer.onResume()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Util.SDK_INT <= 23) {
            binding.videoPlayer.onPause()
            releasePlayer()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStop() {
        super.onStop()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Util.SDK_INT > 23) {
            binding.videoPlayer.onPause()
            releasePlayer()
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when {
            newConfig.orientation == 2 -> {
                //landscape mode
                binding.nonVideoParentConstraintLayout.visibility = View.GONE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.parentConstraintLayout)
                constraintSet.clear(binding.nonVideoParentConstraintLayout.id, ConstraintSet.TOP)
                constraintSet.applyTo(binding.parentConstraintLayout)
                isFullScreen = true
                fullScreenButton.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.portrait_player_icon
                    )
                )
                playerTitleText.visibility = View.VISIBLE
                playerInstructorNameText.visibility = View.VISIBLE
            }

            newConfig.orientation == 1 -> {
                //portrait mode
                binding.nonVideoParentConstraintLayout.visibility = View.VISIBLE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.parentConstraintLayout)
                constraintSet.connect(
                    binding.nonVideoParentConstraintLayout.id,
                    ConstraintSet.TOP,
                    binding.guideline1.id,
                    ConstraintSet.BOTTOM
                )
                constraintSet.applyTo(binding.parentConstraintLayout)
                isFullScreen = false
                fullScreenButton.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.full_screen_player_icon
                    )
                )
                playerTitleText.visibility = View.GONE
                playerInstructorNameText.visibility = View.GONE
            }
        }

    }

    override fun onAnimationCompleted(loopNumber: Int) {
        gifDrawable.reset()
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(
            requireActivity().window,
            binding.videoPlayer
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun shareContent(shareLink: String, contentTitle: String) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, "boolean bear")
        var shareMessage =
            "$contentTitle \n\nWatch it on boolean bear.\n"
        shareMessage =
            shareMessage + shareLink + "\n"
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(intent, "Select One"))
    }
}