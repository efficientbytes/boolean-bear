package app.efficientbytes.booleanbear.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentHomeBinding
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import app.efficientbytes.booleanbear.ui.adapters.InfiniteViewPagerAdapter
import app.efficientbytes.booleanbear.ui.adapters.ReelTopicsChipRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.YoutubeContentViewRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.dummyReelTopicsList
import app.efficientbytes.booleanbear.utils.dummyReelsList
import app.efficientbytes.booleanbear.viewmodels.HomeViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class HomeFragment : Fragment(), ReelTopicsChipRecyclerViewAdapter.OnItemClickListener,
    YoutubeContentViewRecyclerViewAdapter.OnItemClickListener,
    InfiniteViewPagerAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: HomeViewModel by inject()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val infiniteRecyclerAdapter: InfiniteViewPagerAdapter by lazy {
        InfiniteViewPagerAdapter(emptyList(), this@HomeFragment)
    }
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null
    private var currentPage = 1
    private val delay3k: Long = 3000 // Delay in milliseconds
    private val delay5k: Long = 5000 // Delay in milliseconds
    private val authenticationRepository: AuthenticationRepository by inject()
    private val reelTopicsChipRecyclerViewAdapter: ReelTopicsChipRecyclerViewAdapter by lazy {
        ReelTopicsChipRecyclerViewAdapter(dummyReelTopicsList, requireContext(), this@HomeFragment)
    }
    private val reelsRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(dummyReelsList, requireContext(), this@HomeFragment)
    }
    private val connectivityListener: ConnectivityListener by inject()
    private var loginToContinueFragment: LoginToContinueFragment? = null
    private var accountSettingsFragment: AccountSettingsFragment? = null
    private var searchView: SearchView? = null
    private val alternateSearchHint = "Search for tags \"#advanced\""
    private val hintHandler = Handler(Looper.getMainLooper())
    private var isFirstHintText = false
    private var isSearchViewOpen = false
    private var hintRunnable: Runnable? = null

    companion object {

        var selectedReelTopicId = ""
        var selectedReelTopicPosition = -1
        var selectedReelTopic = ""
        var loadingReelTopicsFailed = false
        var loadingReelsFailed = false
        var loadingBannersFailed = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycle.addObserver(viewModel)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //for banner ads
        //for reel topics
        val reelTopicsLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.reelTopicsRecyclerView.layoutManager = reelTopicsLayoutManager
        binding.reelTopicsRecyclerView.adapter = reelTopicsChipRecyclerViewAdapter
        viewModel.reelTopics.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.EmptyResult -> {
                    topicsLoadingFailed()
                }

                DataStatus.Status.Failed -> {
                    topicsLoadingFailed()
                }

                DataStatus.Status.Loading -> {
                    topicsLoading()
                }

                DataStatus.Status.NoInternet -> {
                    loadingReelTopicsFailed = true
                    topicsLoadingFailed()
                }

                DataStatus.Status.Success -> {
                    it.data?.let { list ->
                        if (list.isNotEmpty()) {
                            topicsLoaded()
                            reelTopicsChipRecyclerViewAdapter.setReelTopics(list.subList(0, 5))
                            val firstTopic = list.find { topic -> topic.displayIndex == 1 }
                            if (firstTopic != null) {
                                selectedReelTopic = firstTopic.topic
                                selectedReelTopicId = firstTopic.topicId
                                selectedReelTopicPosition = 0
                                viewModel.getReels(firstTopic.topicId)
                            }
                        } else {
                            topicsLoadingFailed()
                        }
                    }
                }

                DataStatus.Status.TimeOut -> {
                    topicsLoadingFailed()
                }

                DataStatus.Status.UnKnownException -> {
                    topicsLoadingFailed()
                }

                else -> {
                    topicsLoadingFailed()
                }
            }
        }
        binding.reelTopicsRefreshButton.setOnClickListener {
            viewModel.getReelTopics()
        }
        //for reels
        val reelsLayoutManager = LinearLayoutManager(requireContext())
        binding.reelsRecyclerView.layoutManager = reelsLayoutManager
        binding.reelsRecyclerView.adapter = reelsRecyclerViewAdapter
        viewModel.reels.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.EmptyResult -> {
                    emptyReels()
                }

                DataStatus.Status.Failed -> {
                    reelsLoadingFailed()
                }

                DataStatus.Status.Loading -> {
                    reelsLoading()
                }

                DataStatus.Status.NoInternet -> {
                    loadingReelsFailed = true
                    reelsLoadingFailed()
                }

                DataStatus.Status.Success -> {
                    reelsLoaded()
                    it.data?.let { list ->
                        reelsRecyclerViewAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.TimeOut -> {
                    reelsLoadingFailed()
                }

                else -> {
                    reelsLoadingFailed()
                }
            }
        }
        binding.reelsRefreshButton.setOnClickListener {
            viewModel.getReels(selectedReelTopicId)
        }
        //for connectivity listener
        mainViewModel.watchContentIntentInvoked.observe(viewLifecycleOwner) {
            it?.let { contentId ->
                watchContentViaIntent(contentId)
                mainViewModel.resetWatchContentIntentInvoked()
            }
        }
        mainViewModel.deleteAccountIntentInvoked.observe(viewLifecycleOwner) {
            it?.let {
                val directions = HomeFragmentDirections.homeFragmentToDeleteUserAccountFragment()
                findNavController().navigate(directions)
                mainViewModel.resetDeleteAccountIntentInvoked()
            }
        }
    }

    private fun topicsLoadingFailed() {
        binding.reelTopicsRecyclerView.visibility = View.GONE
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.VISIBLE
    }

    private fun topicsLoading() {
        binding.reelTopicsRecyclerView.visibility = View.VISIBLE
        binding.reelsRecyclerView.visibility = View.VISIBLE
        reelTopicsChipRecyclerViewAdapter.setReelTopics(dummyReelTopicsList)
        reelsRecyclerViewAdapter.setYoutubeContentViewList(dummyReelsList)
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
    }

    private fun topicsLoaded() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelTopicsRecyclerView.visibility = View.VISIBLE
    }

    private fun emptyReels() {
        binding.reelsRecyclerView.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.GONE
        binding.noResultLinearLayout.visibility = View.VISIBLE
    }

    private fun reelsLoading() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.VISIBLE
        reelsRecyclerViewAdapter.setYoutubeContentViewList(dummyReelsList)
    }

    private fun reelsLoaded() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.VISIBLE
    }

    private fun reelsLoadingFailed() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelTopicsRefreshButton.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.VISIBLE
    }

    private fun startAutoScroll() {
        if (bannerRunnable == null) {
            bannerRunnable = object : Runnable {
                override fun run() {
                    if (currentPage == (binding.bannerAdsViewPager.adapter?.itemCount ?: 1)) {
                        currentPage = 1
                    }
                    binding.bannerAdsViewPager.setCurrentItem(currentPage++, true)
                    bannerHandler.postDelayed(this, delay3k)
                }
            }
            bannerHandler.postDelayed(bannerRunnable!!, delay3k)
        }
    }

    private fun startAlternateHint() {
        if (hintRunnable == null) {
            hintRunnable = object : Runnable {
                override fun run() {
                    if (isFirstHintText) {
                        searchView?.queryHint = alternateSearchHint
                    } else {
                        searchView?.queryHint =
                            "Search for ${selectedReelTopic.lowercase(Locale.ROOT)} contents"
                    }
                    isFirstHintText = !isFirstHintText
                    hintHandler.postDelayed(this, delay3k)
                }
            }
            hintHandler.postDelayed(hintRunnable!!, 0)
        }
    }

    private fun onInfinitePageChangeCallback(listSize: Int) {
        binding.bannerAdsViewPager.adapter = null
        binding.bannerAdsViewPager.adapter = infiniteRecyclerAdapter
        binding.bannerAdsViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (binding.bannerAdsViewPager.currentItem) {
                        listSize - 1 -> binding.bannerAdsViewPager.setCurrentItem(
                            1,
                            false
                        )

                        0 -> binding.bannerAdsViewPager.setCurrentItem(
                            listSize - 2,
                            false
                        )
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != 0 && position != listSize - 1) {
                    currentPage = position
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            authenticationRepository.listenForAuthStateChanges()
        }
    }

    override fun onStop() {
        super.onStop()
        if (infiniteRecyclerAdapter != null) {
            bannerHandler.removeCallbacksAndMessages(null)
        }
        bannerRunnable = null
        bannerHandler.removeCallbacksAndMessages(null)
        hintRunnable = null
        hintHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (infiniteRecyclerAdapter != null) {
            bannerHandler.removeCallbacksAndMessages(null)
        }
        bannerRunnable = null
        bannerHandler.removeCallbacksAndMessages(null)
        hintRunnable = null
        hintHandler.removeCallbacksAndMessages(null)
    }

    override fun onChipItemClicked(position: Int, remoteReelTopic: RemoteReelTopic) {
        selectedReelTopicPosition = position
        selectedReelTopicId = remoteReelTopic.topicId
        selectedReelTopic = remoteReelTopic.topic
        viewModel.getReels(selectedReelTopicId)
    }

    override fun onChipLastItemClicked() {
        findNavController().navigate(R.id.homeFragment_to_discoverFragment)
    }

    override fun onResume() {
        super.onResume()
        if (selectedReelTopicPosition != -1 && selectedReelTopicId.isNotBlank()) {
            reelTopicsChipRecyclerViewAdapter.checkedPosition = selectedReelTopicPosition
            reelTopicsChipRecyclerViewAdapter.notifyItemChanged(selectedReelTopicPosition)
        }
        startAutoScroll()
        if (isSearchViewOpen) {
            startAlternateHint()
        }
    }

    override fun onYoutubeContentViewItemClicked(
        position: Int,
        remoteReel: RemoteReel
    ) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val directions =
                HomeFragmentDirections.homeFragmentToShuffledContentPlayerFragment(reelId = remoteReel.reelId)
            findNavController().navigate(directions)
        } else {
            if (loginToContinueFragment == null) {
                loginToContinueFragment = LoginToContinueFragment()
            }
            if (!LoginToContinueFragment.isOpened) {
                LoginToContinueFragment.isOpened = true
                loginToContinueFragment!!.show(
                    parentFragmentManager,
                    LoginToContinueFragment.LOGIN_TO_CONTINUE_FRAGMENT
                )
            }
        }
    }

    private fun watchContentViaIntent(contentId: String) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val directions =
                HomeFragmentDirections.homeFragmentToShuffledContentPlayerFragment(contentId)
            findNavController().navigate(directions)
        } else {
            if (loginToContinueFragment == null) {
                loginToContinueFragment = LoginToContinueFragment()
            }
            if (!LoginToContinueFragment.isOpened) {
                LoginToContinueFragment.isOpened = true
                loginToContinueFragment!!.show(
                    parentFragmentManager,
                    LoginToContinueFragment.LOGIN_TO_CONTINUE_FRAGMENT
                )
            }
        }
    }

    override fun onBannerClicked(position: Int, remoteHomePageBanner: RemoteHomePageBanner) {
        if (remoteHomePageBanner.clickAble) {
            val link = remoteHomePageBanner.redirectLink
            if (!link.isNullOrBlank()) {
                val uri = Uri.parse(link)
                val domain = uri.host
                if (domain == "app.booleanbear.com") {
                    val pathSegments = uri.pathSegments
                    if (pathSegments.size >= 2) {
                        val watchSegment = pathSegments[1]
                        when (watchSegment.getOrNull(0)) {
                            'v' -> {
                                val contentId = pathSegments.lastOrNull()
                                if (contentId != null) {
                                    watchContentViaIntent(contentId)
                                }
                            }

                            else -> {

                            }
                        }
                    }
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    requireContext().startActivity(intent)
                }
            }
        }
    }
}