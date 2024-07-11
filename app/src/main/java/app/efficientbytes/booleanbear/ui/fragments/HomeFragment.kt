package app.efficientbytes.booleanbear.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import app.efficientbytes.booleanbear.utils.dummyHomePageBannersList
import app.efficientbytes.booleanbear.utils.dummyReelTopicsList
import app.efficientbytes.booleanbear.utils.dummyReelsList
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
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
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val viewModel: HomeViewModel by viewModels()
    private val delay3k: Long = 3000 // Delay in milliseconds
    private val delay5k: Long = 5000 // Delay in milliseconds
    private val authenticationRepository: AuthenticationRepository by inject()
    private val bannerViewPagerAdapter: InfiniteViewPagerAdapter by lazy {
        InfiniteViewPagerAdapter(dummyHomePageBannersList, this@HomeFragment)
    }
    private val reelTopicsChipRecyclerViewAdapter: ReelTopicsChipRecyclerViewAdapter by lazy {
        ReelTopicsChipRecyclerViewAdapter(dummyReelTopicsList, requireContext(), this@HomeFragment)
    }
    private val reelsRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(dummyReelsList, requireContext(), this@HomeFragment)
    }
    private val searchResultRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
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

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_toolbar, menu)
                val search = menu.findItem(R.id.searchMenu)
                search.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                        requireActivity().invalidateOptionsMenu()
                        isSearchViewOpen = false
                        hintHandler.removeCallbacksAndMessages(null)
                        hintRunnable = null
                        binding.searchResultsParentConstraintLayout.visibility = View.GONE
                        binding.mainContentParentNestedScrollView.visibility = View.VISIBLE
                        return true
                    }

                })
                searchView = search.actionView as? SearchView
                val linearLayout1 = searchView!!.getChildAt(0) as LinearLayout
                val linearLayout2 = linearLayout1.getChildAt(2) as LinearLayout
                val linearLayout3 = linearLayout2.getChildAt(1) as LinearLayout
                val searchEditText = linearLayout3.getChildAt(0) as TextView
                searchEditText.textSize = 16f
                searchEditText.setTextColor(
                    AppCompatResources.getColorStateList(
                        requireContext(),
                        R.color.white
                    )
                )
                searchView?.setOnSearchClickListener {
                    menu.findItem(R.id.accountSettingsMenu).setVisible(false)
                    menu.findItem(R.id.discoverMenu).setVisible(false)
                    isSearchViewOpen = true
                    startAlternateHint()
                    binding.mainContentParentNestedScrollView.visibility = View.GONE
                    binding.searchResultsParentConstraintLayout.visibility = View.VISIBLE
                    binding.searchResultsNoResultParentConstraintLayout.visibility = View.GONE
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    viewModel.getReelQueries(selectedReelTopicId)
                }
                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.apply {
                            viewModel.getReelQueries(selectedReelTopicId, this)
                        }
                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        query?.apply {
                            if (!query.startsWith("#")) {
                                viewModel.getReelQueries(selectedReelTopicId, this)
                            } else {
                                binding.searchResultsNoResultParentConstraintLayout.visibility =
                                    View.GONE
                                binding.searchResultsRecyclerView.visibility = View.GONE
                            }
                        }
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.accountSettingsMenu -> {
                        if (accountSettingsFragment == null) {
                            accountSettingsFragment = AccountSettingsFragment()
                        }
                        if (!AccountSettingsFragment.isOpened) {
                            AccountSettingsFragment.isOpened = true
                            accountSettingsFragment!!.show(
                                parentFragmentManager,
                                AccountSettingsFragment.ACCOUNT_SETTINGS_FRAGMENT
                            )
                        }
                        return true
                    }

                    R.id.discoverMenu -> {
                        findNavController().navigate(R.id.homeFragment_to_discoverFragment)
                        return true
                    }

                    R.id.searchMenu -> {
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
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
                            val topicsToShow = if (list.size > 5) 5 else list.size
                            reelTopicsChipRecyclerViewAdapter.setReelTopics(
                                list.subList(
                                    0,
                                    topicsToShow
                                )
                            )
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

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

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
            if (!isSearchViewOpen) binding.mainContentParentNestedScrollView.visibility =
                View.VISIBLE
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
                    it.data?.let { list ->
                        reelsLoaded()
                        reelsRecyclerViewAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.TimeOut -> {
                    reelsLoadingFailed()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    reelsLoadingFailed()
                }
            }
        }
        binding.reelsRefreshButton.setOnClickListener {
            viewModel.getReels(selectedReelTopicId)
        }
        //for search result
        val searchResultLayoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecyclerView.layoutManager = searchResultLayoutManager
        binding.searchResultsRecyclerView.adapter = searchResultRecyclerViewAdapter
        viewModel.searchResult.observe(viewLifecycleOwner) {
            if (isSearchViewOpen) binding.searchResultsParentConstraintLayout.visibility =
                View.VISIBLE
            when (it.status) {
                DataStatus.Status.EmptyResult -> {
                    searchEmptyResult()
                }

                DataStatus.Status.Loading -> {
                    searchResultSuccess()
                    searchResultRecyclerViewAdapter.setYoutubeContentViewList(dummyReelsList)
                }

                DataStatus.Status.Success -> {
                    it.data?.let { list ->
                        searchResultSuccess()
                        searchResultRecyclerViewAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    searchEmptyResult()
                }
            }
        }
        //for connectivity listener
        connectivityListener.observe(viewLifecycleOwner) { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (loadingReelTopicsFailed) {
                        loadingReelTopicsFailed = false
                        viewModel.getReelTopics()
                    }
                    if (loadingReelsFailed) {
                        loadingReelsFailed = false
                        viewModel.getReels(selectedReelTopicId)
                    }
                    if (loadingBannersFailed) {
                        loadingBannersFailed = false
                    }
                }

                false -> {

                }
            }
        }

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
        mainViewModel.getFirebaseUserToken()
        mainViewModel.firebaseUserToken.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    it.data?.let { token ->
                        val isEmailVerified = token.claims["emailVerified"]
                        if (isEmailVerified is Boolean && isEmailVerified == false) {
                            val directions =
                                HomeFragmentDirections.homeFragmentToVerifyPrimaryEmailFragment(1)
                            findNavController().navigate(directions)
                        }
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {

                }
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
        binding.reelsRecyclerView.adapter = null
        reelsRecyclerViewAdapter.setYoutubeContentViewList(dummyReelsList)
        binding.reelsRecyclerView.adapter = reelsRecyclerViewAdapter
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

    private fun searchEmptyResult() {
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchResultsNoResultParentConstraintLayout.visibility = View.VISIBLE
    }

    private fun searchResultSuccess() {
        binding.searchResultsNoResultParentConstraintLayout.visibility = View.GONE
        binding.searchResultsRecyclerView.visibility = View.VISIBLE
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

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        hintRunnable = null
        hintHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        hintRunnable = null
        hintHandler.removeCallbacksAndMessages(null)
    }

    override fun onChipItemClicked(position: Int, remoteReelTopic: RemoteReelTopic) {
        loadingReelsFailed = false
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            authenticationRepository.getLiveAuthStateFromRemote()
        }
        if (selectedReelTopicPosition != -1 && selectedReelTopicId.isNotBlank()) {
            reelTopicsChipRecyclerViewAdapter.checkedPosition = selectedReelTopicPosition
            reelTopicsChipRecyclerViewAdapter.notifyItemChanged(selectedReelTopicPosition)
        }
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

    override fun onDetach() {
        super.onDetach()
        hintRunnable = null
        hintHandler.removeCallbacksAndMessages(null)
    }
}