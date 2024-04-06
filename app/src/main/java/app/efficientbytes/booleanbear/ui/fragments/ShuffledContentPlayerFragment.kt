package app.efficientbytes.booleanbear.ui.fragments

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.textview.MaterialTextView
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.net.UnknownHostException

class ShuffledContentPlayerFragment : Fragment(), AnimationListener {

    private val TAG = this.javaClass.simpleName
    private lateinit var _binding: FragmentShuffledContentPlayerBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private var isFullScreen = false
    private lateinit var playerTitleText: MaterialTextView
    private lateinit var playerInstructorNameText: MaterialTextView
    private var trackDialog: Dialog? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var isFailed = false
    private lateinit var playerQualityMenu: LinearLayout
    private lateinit var fullScreenButton: ImageButton
    private lateinit var gifDrawable: GifDrawable
    private lateinit var contentId: String
    private var mediaItem: MediaItem? = null

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
        return rootView
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gifDrawable = binding.booleanBearLoadingGif.drawable as GifDrawable
        gifDrawable.addAnimationListener(this@ShuffledContentPlayerFragment)

        binding.shimmerTopContentShimmerLayout.visibility = View.VISIBLE
        binding.shimmerTopContentShimmerLayout.startShimmer()
        binding.shimmerSponsorShimmerLayout.visibility = View.VISIBLE
        binding.shimmerSponsorShimmerLayout.startShimmer()

        fullScreenButton = rootView.findViewById(R.id.playerFullScreenImageButton)
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

        val closeButton = rootView.findViewById<ImageButton>(R.id.playerCancelAndGoBackImageButton)
        closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        playerTitleText = rootView.findViewById(R.id.playerTitleValueTextView)
        playerTitleText.text = "What are kotlin scope functions"

        playerInstructorNameText = rootView.findViewById(R.id.playerInstructorNameValueTextView)
        playerInstructorNameText.text = "Anubhav.P.S"

        playerQualityMenu = rootView.findViewById(R.id.playerQualityMenuLinearLayout)

        if (isFullScreen) {
            playerTitleText.visibility = View.VISIBLE
            playerInstructorNameText.visibility = View.VISIBLE
        } else {
            playerTitleText.visibility = View.GONE
            playerInstructorNameText.visibility = View.GONE
        }

        val playerQualityButton = rootView.findViewById<ImageButton>(R.id.exo_track_selection_view)
        playerQualityButton.setOnClickListener {
        }

        binding.retryButton.setOnClickListener {
            initializePlayer()
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        trackSelector = DefaultTrackSelector(requireContext())
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setAllowVideoMixedMimeTypeAdaptiveness(true)
        )

        val mediaItem = if (!isFailed) {
            MediaItem.fromUri("https://vz-5f0cfb49-882.b-cdn.net/bcdn_token=yyCAHHrsV933mGpfg8S1q-JvKn2uPw2SBYYwGa-CzS8&token_countries=IN&token_path=%2F150c236a-b9e0-4239-9cb4-8dfdedda46cd%2F&expires=1712336901/150c236a-b9e0-4239-9cb4-8dfdedda46cd/playlist.m3u8")
        } else {
            MediaItem.fromUri("https://vz-5f0cfb49-882.b-cdn.net/bcdn_token=PxJhK2GYX-eGURXi-tEA7T8Z4SN7_kUyv_HV06bPJt8&token_countries=IN&token_path=%2F150c236a-b9e0-4239-9cb4-8dfdedda46cd%2F&expires=1712400667/150c236a-b9e0-4239-9cb4-8dfdedda46cd/playlist.m3u8")
        }

        if (player == null) {
            val loadControl = DefaultLoadControl()
            player = ExoPlayer.Builder(requireContext())
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build()
                .also { exoPlayer ->
                    exoPlayer.playWhenReady = playWhenReady
                    exoPlayer.addListener(playbackStateListener)
                    binding.videoPlayer.player = exoPlayer
                    binding.videoPlayer.keepScreenOn = true
                }
        }
        val haveStartPosition = mediaItemIndex != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(mediaItemIndex, playbackPosition)
        }
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaSource =
            HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)
        player!!.setMediaSource(mediaSource,  /* resetPosition= */!haveStartPosition)
        player!!.prepare()
        return
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "Screen orientation value is ${newConfig.orientation}")
        when {
            newConfig.orientation == 2 -> {
                //landscape mode
                binding.contentConstraintLayout.visibility = View.GONE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.parentConstraintLayout)
                constraintSet.clear(binding.contentConstraintLayout.id, ConstraintSet.TOP)
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
                binding.contentConstraintLayout.visibility = View.VISIBLE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.parentConstraintLayout)
                constraintSet.connect(
                    binding.contentConstraintLayout.id,
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
                    binding.videoPlayer.showController()
                    binding.noNetworkLinearLayout.visibility = View.GONE
                    playerQualityMenu.visibility = View.VISIBLE
                    gifDrawable.stop()
                    binding.booleanBearLoadingGif.visibility = View.GONE
                }

                ExoPlayer.STATE_ENDED -> {
                    binding.videoPlayer.showController()
                    playerQualityMenu.visibility = View.GONE
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
                val httpError = cause
                if (httpError is HttpDataSource.InvalidResponseCodeException) {
                    if (httpError.responseCode == 403) {
                        isFailed = true
                        initializePlayer()
                    }

                } else {
                    if (httpError.cause is UnknownHostException) {
                        gifDrawable.stop()
                        binding.booleanBearLoadingGif.visibility = View.GONE
                        binding.noNetworkLinearLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onAnimationCompleted(loopNumber: Int) {
        gifDrawable.reset()
    }
}