package app.efficientbytes.booleanbear.ui.fragments

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentReelPlayerBinding
import app.efficientbytes.booleanbear.models.VideoPlaybackSpeed
import app.efficientbytes.booleanbear.models.VideoQualityType
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.createShareIntent
import app.efficientbytes.booleanbear.viewmodels.ReelPlayerViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.net.UnknownHostException

class ReelPlayerFragment : Fragment(), AnimationListener {

    private lateinit var _binding: FragmentReelPlayerBinding
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
    private lateinit var playerGoToHomePageText: MaterialTextView
    private lateinit var playerQualityMenu: LinearLayout
    private lateinit var fullScreenButton: ImageButton
    private lateinit var gifDrawable: GifDrawable
    private lateinit var reelId: String
    private lateinit var playerQualityButton: ImageButton
    private lateinit var playerPlaybackSpeedButton: ImageButton
    private lateinit var playerQualityPortraitText: MaterialTextView
    private lateinit var playerSpeedPortraitText: MaterialTextView
    private lateinit var playerCloseButton: ImageButton
    private var dialog: Dialog? = null

    //flags
    private var isFullScreen = false
    private var isPlayingSuggested = false
    private val viewModel: ReelPlayerViewModel by inject()
    private var nextSuggestedContentId: String? = null
    private var noInternet = false
    private val connectivityListener: ConnectivityListener by inject()
    private var reelsDescriptionFragment: ReelsDescriptionFragment? = null
    private var contentTitle: String = ""
    private var isPlayerQualityOrSpeedDialogOpened: Boolean = false
    private var currentVideoQuality = VideoQualityType.AUTO
    private var currentPlaybackSpeed = VideoPlaybackSpeed.x1
    private val customAuthStateListener: CustomAuthStateListener by inject()
    private val safeArgs: ReelPlayerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReelPlayerBinding.inflate(inflater, container, false)
        rootView = binding.root
        lifecycle.addObserver(viewModel)
        return rootView
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.reelId = safeArgs.reelId

        if (FirebaseAuth.getInstance().currentUser == null) {
            findNavController().popBackStack()
            return
        }

        gifDrawable = binding.booleanBearLoadingGif.drawable as GifDrawable
        gifDrawable.addAnimationListener(this@ReelPlayerFragment)
        playerTitleText = rootView.findViewById(R.id.playerTitleValueTextView)
        playerGoToHomePageText = rootView.findViewById(R.id.playerGoToHomePage)
        playerQualityMenu = rootView.findViewById(R.id.playerQualityMenuLinearLayout)
        playerCloseButton =
            rootView.findViewById<ImageButton>(R.id.playerCancelAndGoBackImageButton)
        playerQualityButton =
            rootView.findViewById<ImageButton>(R.id.playerQualityImageButton)
        playerPlaybackSpeedButton =
            rootView.findViewById<ImageButton>(R.id.playerPortraitChooseSpeedImageButton)
        fullScreenButton = rootView.findViewById(R.id.playerFullScreenImageButton)

        playerQualityPortraitText = rootView.findViewById(R.id.playerPortraitQualityValueTextView)
        playerSpeedPortraitText = rootView.findViewById(R.id.playerPortraitSpeedValueTextView)

        playerQualityPortraitText.text = currentVideoQuality.label

        if (!connectivityListener.isInternetAvailable()) {
            binding.parentConstraintLayout.visibility = View.GONE
            binding.noInternetLinearLayout.visibility = View.VISIBLE
        } else {
            binding.noInternetLinearLayout.visibility = View.GONE
            binding.parentConstraintLayout.visibility = View.VISIBLE
            viewModel.getReelPlayLink(reelId)
            viewModel.getReelDetails(reelId)
        }

        viewModel.reelPlayLink.observe(viewLifecycleOwner) {
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
                        mediaItem = MediaItem.fromUri(Uri.parse(playUrl))
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

        viewModel.reelDetails.observe(viewLifecycleOwner) {
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
                        playDetails.nextReelId?.let { suggestedContentId ->
                            nextSuggestedContentId = suggestedContentId
                            viewModel.getSuggestedContent(suggestedContentId)
                        }
                        viewModel.getInstructorProfile(playDetails.instructorId)
                        playDetails.mentionedLinkIds?.let { mentionedLinkIds ->
                            viewModel.getMentionedLinks(
                                mentionedLinkIds
                            )
                        }
                        if (playDetails.nextReelId == null) {
                            binding.shimmerSuggestedContent.stopShimmer()
                            binding.shimmerSuggestedContent.visibility = View.GONE
                            binding.suggestedContentCardView.visibility = View.GONE
                            nextSuggestedContentId = null
                        }
                        binding.reelDetails = playDetails
                        playerTitleText.text = playDetails.title
                        contentTitle = playDetails.title
                        val instructorFullName = if (playDetails.instructorLastName == null) {
                            playDetails.instructorFirstName
                        } else {
                            playDetails.instructorFirstName + " " + playDetails.instructorLastName
                        }
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

                DataStatus.Status.UnKnownException -> {
                    //show error
                    //snack bar
                }

                else -> {

                }
            }
        }

        viewModel.nextReel.observe(viewLifecycleOwner) {
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
                        binding.nextReel = youtubeContentView
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

                DataStatus.Status.UnKnownException -> {
                    //show error
                    //snack-bar
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

        playerCloseButton.setOnClickListener {
            findNavController().popBackStack()
        }

        playerGoToHomePageText.setOnClickListener {
            findNavController().popBackStack()
        }

        if (isFullScreen) {
            playerTitleText.visibility = View.VISIBLE
            playerGoToHomePageText.visibility = View.VISIBLE
            playerCloseButton.visibility = View.INVISIBLE
        } else {
            playerTitleText.visibility = View.GONE
            playerGoToHomePageText.visibility = View.GONE
            playerCloseButton.visibility = View.VISIBLE
        }

        playerQualityButton.setOnClickListener {
            playerQualitySelectorDialog()
        }

        playerPlaybackSpeedButton.setOnClickListener {
            playerPlaybackSpeedSelectorDialog()
        }

        binding.retryButton.setOnClickListener {
            if (noInternet) {
                noInternet = false
                viewModel.getReelPlayLink(reelId)
                viewModel.getReelDetails(reelId)
            } else {
                initializePlayer()
            }
        }

        binding.suggestedContentCardView.setOnClickListener {
            nextSuggestedContentId?.let { reelId ->
                clearStartPosition()
                isPlayingSuggested = true
                this@ReelPlayerFragment.reelId = reelId
                ReelPlayerViewModel.countRecorded = false
                binding.videoPlayer.hideController()
                playerQualityMenu.visibility = View.GONE
                viewModel.getReelPlayLink(reelId)
                viewModel.getReelDetails(reelId)
            }
        }

        binding.retryAfterNoInternetButton.setOnClickListener {
            if (connectivityListener.isInternetAvailable()) {
                noInternet = false
                binding.noInternetLinearLayout.visibility = View.GONE
                binding.parentConstraintLayout.visibility = View.VISIBLE
                viewModel.getReelPlayLink(reelId)
                viewModel.getReelDetails(reelId)
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
                    ReelPlayerViewModel.countRecorded = false
                }

                DataStatus.Status.TimeOut -> {
                    ReelPlayerViewModel.countRecorded = false
                }

                DataStatus.Status.Success -> {
                    ReelPlayerViewModel.countRecorded = true
                }

                else -> {}
            }
        }
        binding.shareContentLabelTextView.setOnClickListener {
            val shareLink = "https://app.booleanbear.com/watch/content/$reelId"
            val shareMessage = "$contentTitle \n\nWatch it on boolean bear.\n"
            val shareIntent = createShareIntent(shareLink, shareMessage)
            startActivity(shareIntent)
        }

        customAuthStateListener.liveData.observe(viewLifecycleOwner) {
            when (it) {
                true -> {

                }

                false -> {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }
    }

    private fun openDescriptionFragment() {
        if (reelsDescriptionFragment == null) {
            reelsDescriptionFragment = ReelsDescriptionFragment()
        }
        if (!ReelsDescriptionFragment.isOpened) {
            ReelsDescriptionFragment.isOpened = true
            reelsDescriptionFragment!!.show(
                parentFragmentManager,
                ReelsDescriptionFragment.SHUFFLED_DESCRIPTION_FRAGMENT
            )
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        if (player == null) {
            trackSelector = DefaultTrackSelector(requireContext(), AdaptiveTrackSelection.Factory())
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
                    if (!ReelPlayerViewModel.countRecorded) {
                        ReelPlayerViewModel.countRecorded = true
                        viewModel.increaseContentViewCount(this@ReelPlayerFragment.reelId)
                        viewModel.addToWatchHistory(this@ReelPlayerFragment.reelId)
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
                    nextSuggestedContentId?.let { reelId ->
                        binding.videoPlayer.hideController()
                        clearStartPosition()
                        isPlayingSuggested = true
                        this@ReelPlayerFragment.reelId = reelId
                        ReelPlayerViewModel.countRecorded = false
                        viewModel.getReelPlayLink(reelId)
                        viewModel.getReelDetails(reelId)
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
                        viewModel.getReelPlayLink(reelId)
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

        @OptIn(UnstableApi::class)
        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
        }
    }

    @UnstableApi
    private fun playerQualitySelectorDialog() {
        if (dialog == null) {
            dialog = Dialog(requireContext())
        }

        if (!isPlayerQualityOrSpeedDialogOpened) {
            isPlayerQualityOrSpeedDialogOpened = true

            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setContentView(R.layout.dialog_video_quality_selector)
            dialog!!.setCanceledOnTouchOutside(true)
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.attributes.windowAnimations = R.style.BottomDialogAnimation
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
            val playerQualityChipGroup = dialog!!.findViewById<ChipGroup>(R.id.qualityChipGroup)
            val playerCurrentQualityText =
                dialog!!.findViewById<MaterialTextView>(R.id.currentQualityValueTextView)

            when (currentVideoQuality) {
                VideoQualityType.AUTO -> {
                    playerCurrentQualityText.text = VideoQualityType.AUTO.label
                    dialog!!.findViewById<Chip>(R.id.qualityAutoChip).isChecked =
                        true
                }

                VideoQualityType._480P -> {
                    playerCurrentQualityText.text = VideoQualityType._480P.label
                    dialog!!.findViewById<Chip>(R.id.quality480pChip).isChecked = true
                }

                VideoQualityType._720P -> {
                    playerCurrentQualityText.text = VideoQualityType._720P.label
                    dialog!!.findViewById<Chip>(R.id.quality720pChip).isChecked =
                        true
                }

                else -> {

                }
            }

            playerQualityChipGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.quality480pChip -> {
                        currentVideoQuality = VideoQualityType._480P
                        playerQualityPortraitText.text = currentVideoQuality.label
                        playerCurrentQualityText.text = currentVideoQuality.label
                        changePlayerQuality(currentVideoQuality)
                        dialog!!.dismiss()
                    }

                    R.id.quality720pChip -> {
                        currentVideoQuality = VideoQualityType._720P
                        playerQualityPortraitText.text = currentVideoQuality.label
                        playerCurrentQualityText.text = currentVideoQuality.label
                        changePlayerQuality(currentVideoQuality)
                        dialog!!.dismiss()
                    }

                    R.id.qualityAutoChip -> {
                        currentVideoQuality = VideoQualityType.AUTO
                        playerQualityPortraitText.text = currentVideoQuality.label
                        playerCurrentQualityText.text = currentVideoQuality.label
                        changePlayerQuality(currentVideoQuality)
                        dialog!!.dismiss()
                    }
                }
            }

            dialog!!.setOnDismissListener {
                isPlayerQualityOrSpeedDialogOpened = false
                dialog = null
            }

            dialog!!.show()
        }

    }

    @UnstableApi
    private fun playerPlaybackSpeedSelectorDialog() {
        if (dialog == null) {
            dialog = Dialog(requireContext())
        }

        if (!isPlayerQualityOrSpeedDialogOpened) {
            isPlayerQualityOrSpeedDialogOpened = true

            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setContentView(R.layout.dialog_video_playback_speed_selector)
            dialog!!.setCanceledOnTouchOutside(true)
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.attributes.windowAnimations = R.style.BottomDialogAnimation
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
            val playerPlaybackSpeedChipGroup =
                dialog!!.findViewById<ChipGroup>(R.id.playbackSpeedChipGroup)
            val playerCurrentPlaybackSpeedText =
                dialog!!.findViewById<MaterialTextView>(R.id.currentPlaybackSpeedValueTextView)

            when (currentPlaybackSpeed) {
                VideoPlaybackSpeed.x0_5 -> {
                    playerCurrentPlaybackSpeedText.text = VideoPlaybackSpeed.x0_5.label
                    dialog!!.findViewById<Chip>(R.id.x0_5Chip).isChecked = true
                }

                VideoPlaybackSpeed.x0_75 -> {
                    playerCurrentPlaybackSpeedText.text = VideoPlaybackSpeed.x0_75.label
                    dialog!!.findViewById<Chip>(R.id.x0_75Chip).isChecked = true
                }

                VideoPlaybackSpeed.x1 -> {
                    playerCurrentPlaybackSpeedText.text = VideoPlaybackSpeed.x1.label
                    dialog!!.findViewById<Chip>(R.id.x1Chip).isChecked = true
                }

                VideoPlaybackSpeed.x1_25 -> {
                    playerCurrentPlaybackSpeedText.text = VideoPlaybackSpeed.x1_25.label
                    dialog!!.findViewById<Chip>(R.id.x1_25Chip).isChecked = true
                }

                VideoPlaybackSpeed.x1_5 -> {
                    playerCurrentPlaybackSpeedText.text = VideoPlaybackSpeed.x1_5.label
                    dialog!!.findViewById<Chip>(R.id.x1_5Chip).isChecked = true
                }

                VideoPlaybackSpeed.x1_75 -> {
                    playerCurrentPlaybackSpeedText.text = VideoPlaybackSpeed.x1_75.label
                    dialog!!.findViewById<Chip>(R.id.x1_75Chip).isChecked = true
                }
            }

            playerPlaybackSpeedChipGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.x0_5Chip -> {
                        currentPlaybackSpeed = VideoPlaybackSpeed.x0_5
                        playerSpeedPortraitText.text = currentPlaybackSpeed.label
                        playerCurrentPlaybackSpeedText.text = currentPlaybackSpeed.label
                        changePlayerPlaybackSpeed(currentPlaybackSpeed)
                        dialog!!.dismiss()
                    }

                    R.id.x0_75Chip -> {
                        currentPlaybackSpeed = VideoPlaybackSpeed.x0_75
                        playerSpeedPortraitText.text = currentPlaybackSpeed.label
                        playerCurrentPlaybackSpeedText.text = currentPlaybackSpeed.label
                        changePlayerPlaybackSpeed(currentPlaybackSpeed)
                        dialog!!.dismiss()
                    }

                    R.id.x1Chip -> {
                        currentPlaybackSpeed = VideoPlaybackSpeed.x1
                        playerSpeedPortraitText.text = currentPlaybackSpeed.label
                        playerCurrentPlaybackSpeedText.text = currentPlaybackSpeed.label
                        changePlayerPlaybackSpeed(currentPlaybackSpeed)
                        dialog!!.dismiss()
                    }

                    R.id.x1_25Chip -> {
                        currentPlaybackSpeed = VideoPlaybackSpeed.x1_25
                        playerSpeedPortraitText.text = currentPlaybackSpeed.label
                        playerCurrentPlaybackSpeedText.text = currentPlaybackSpeed.label
                        changePlayerPlaybackSpeed(currentPlaybackSpeed)
                        dialog!!.dismiss()
                    }

                    R.id.x1_5Chip -> {
                        currentPlaybackSpeed = VideoPlaybackSpeed.x1_5
                        playerSpeedPortraitText.text = currentPlaybackSpeed.label
                        playerCurrentPlaybackSpeedText.text = currentPlaybackSpeed.label
                        changePlayerPlaybackSpeed(currentPlaybackSpeed)
                        dialog!!.dismiss()
                    }

                    R.id.x1_75Chip -> {
                        currentPlaybackSpeed = VideoPlaybackSpeed.x1_75
                        playerSpeedPortraitText.text = currentPlaybackSpeed.label
                        playerCurrentPlaybackSpeedText.text = currentPlaybackSpeed.label
                        changePlayerPlaybackSpeed(currentPlaybackSpeed)
                        dialog!!.dismiss()
                    }
                }
            }

            dialog!!.setOnDismissListener {
                isPlayerQualityOrSpeedDialogOpened = false
                dialog = null
            }

            dialog!!.show()
        }

    }

    @UnstableApi
    private fun changePlayerQuality(videoQualityType: VideoQualityType) {
        trackSelector = trackSelector.apply {
            setParameters(
                buildUponParameters().setMaxVideoSize(
                    videoQualityType.width,
                    videoQualityType.height
                ).setAllowVideoNonSeamlessAdaptiveness(true)
            )
        }
        player!!.prepare()
    }

    @UnstableApi
    private fun changePlayerPlaybackSpeed(playbackSpeed: VideoPlaybackSpeed) {
        player!!.setPlaybackSpeed(playbackSpeed.speed)
        player!!.prepare()
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (Util.SDK_INT > 23) {
                initializePlayer()
                binding.videoPlayer.onResume()
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser != null) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if ((Util.SDK_INT <= 23 || player == null)) {
                initializePlayer()
                binding.videoPlayer.onResume()
            }
        } else {
            findNavController().popBackStack()
            return
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
                playerGoToHomePageText.visibility = View.VISIBLE
                playerCloseButton.visibility = View.INVISIBLE
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
                playerGoToHomePageText.visibility = View.GONE
                playerCloseButton.visibility = View.VISIBLE
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
}