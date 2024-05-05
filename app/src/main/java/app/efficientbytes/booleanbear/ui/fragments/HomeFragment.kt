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
import app.efficientbytes.booleanbear.ui.adapters.HomeFragmentChipRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.InfiniteViewPagerAdapter
import app.efficientbytes.booleanbear.ui.adapters.YoutubeContentViewRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.viewmodels.HomeViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class HomeFragment : Fragment(), HomeFragmentChipRecyclerViewAdapter.OnItemClickListener,
    YoutubeContentViewRecyclerViewAdapter.OnItemClickListener,
    InfiniteViewPagerAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: HomeViewModel by inject()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private var infiniteRecyclerAdapter: InfiniteViewPagerAdapter? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 1
    private val delay3k: Long = 3000 // Delay in milliseconds
    private val delay5k: Long = 5000 // Delay in milliseconds
    private val authenticationRepository: AuthenticationRepository by inject()
    private val homeFragmentChipRecyclerViewAdapter: HomeFragmentChipRecyclerViewAdapter by lazy {
        HomeFragmentChipRecyclerViewAdapter(emptyList(), requireContext(), this@HomeFragment)
    }
    private val youtubeContentViewRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(emptyList(), requireContext(), this@HomeFragment)
    }
    private lateinit var searchRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter
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
                menuInflater.inflate(R.menu.toolbar_account_menu, menu)
                val search = menu.findItem(R.id.searchMenu)
                search.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                        requireActivity().invalidateOptionsMenu()
                        isSearchViewOpen = false
                        hintHandler.removeCallbacksAndMessages(null)
                        binding.searchConstraintParentLayout.visibility = View.GONE
                        binding.searchViewRecyclerView.adapter = null
                        binding.appBarLayout.visibility = View.VISIBLE
                        binding.contentParentConstraintLayout.visibility = View.VISIBLE
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
                searchView?.maxWidth = Integer.MAX_VALUE
                searchView?.setOnSearchClickListener {
                    menu.findItem(R.id.accountSettingsMenu).setVisible(false)
                    menu.findItem(R.id.discoverMenu).setVisible(false)
                    isSearchViewOpen = true
                    startAlternateHint()
                    binding.appBarLayout.visibility = View.GONE
                    binding.contentParentConstraintLayout.visibility = View.GONE
                    binding.searchConstraintParentLayout.visibility = View.VISIBLE
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
                                binding.searchViewRecyclerView.visibility = View.GONE
                                binding.searchViewNoSearchResultConstraintLayout.visibility =
                                    View.GONE
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
        //set reel topics recycler view
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        viewModel.reelTopics.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.EmptyResult -> {
                    //there is some problem
                    //show retry button

                }

                DataStatus.Status.Failed -> {
                    //there is some problem
                    //show retry button

                }

                DataStatus.Status.Loading -> {
                    binding.contentCategoryShimmerScrollView.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE

                }

                DataStatus.Status.NoInternet -> {
                    loadingReelTopicsFailed = true
                    //show no internet
                    //show retry button
                    binding.contentCategoryShimmerScrollView.visibility = View.GONE
                    binding.categoriesRecyclerView.visibility = View.GONE

                }

                DataStatus.Status.Success -> {
                    binding.contentCategoryShimmerScrollView.visibility = View.GONE
                    binding.categoriesRecyclerView.visibility = View.VISIBLE
                    val list = it.data
                    if (!list.isNullOrEmpty()) {
                        homeFragmentChipRecyclerViewAdapter.setReelTopics(list.subList(0, 5))
                        binding.categoriesRecyclerView.adapter =
                            homeFragmentChipRecyclerViewAdapter
                        val firstTopic = list.find { topic -> topic.displayIndex == 1 }
                        if (firstTopic != null) {
                            selectedReelTopic = firstTopic.topic
                            selectedReelTopicId = firstTopic.topicId
                            selectedReelTopicPosition = 0
                            viewModel.getReels(firstTopic.topicId)
                        }
                    }

                }

                DataStatus.Status.TimeOut -> {
                    loadingReelTopicsFailed = true
                    //show time out
                    //show retry button
                    binding.contentCategoryShimmerScrollView.visibility = View.GONE
                    binding.categoriesRecyclerView.visibility = View.GONE

                }

                DataStatus.Status.UnKnownException -> {
                    //there is some problem
                    //show retry button
                    //show snack bar with indefinite
                    binding.contentCategoryShimmerScrollView.visibility = View.GONE
                    binding.categoriesRecyclerView.visibility = View.GONE

                }

                else -> {
                    //there is some problem
                    //show retry button
                    binding.contentCategoryShimmerScrollView.visibility = View.GONE
                    binding.categoriesRecyclerView.visibility = View.GONE

                }
            }
        }
        //set reels recycler view
        binding.contentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.reels.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.contentsRecyclerView.visibility = View.GONE
                    binding.noSearchResultConstraintLayout.visibility = View.GONE
                }

                DataStatus.Status.Loading -> {
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.noSearchResultConstraintLayout.visibility = View.GONE
                    binding.contentsRecyclerView.visibility = View.GONE
                    binding.shimmerLayoutNestedScrollView.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                }

                DataStatus.Status.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noSearchResultConstraintLayout.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.contentsRecyclerView.visibility = View.VISIBLE
                    it.data?.let { list ->
                        youtubeContentViewRecyclerViewAdapter.setYoutubeContentViewList(list)
                    }
                    binding.contentsRecyclerView.adapter = youtubeContentViewRecyclerViewAdapter
                }

                DataStatus.Status.EmptyResult -> {
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.contentsRecyclerView.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noSearchResultConstraintLayout.visibility = View.VISIBLE
                }

                DataStatus.Status.NoInternet -> {
                    loadingReelsFailed = true
                    binding.contentsRecyclerView.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noSearchResultConstraintLayout.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.VISIBLE
                }

                else -> {

                }
            }
        }
        //set search recycler view
        binding.searchConstraintParentLayout.visibility = View.GONE
        binding.searchViewRecyclerView.adapter = null
        binding.searchViewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.searchResult.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    binding.searchViewNoSearchResultConstraintLayout.visibility = View.GONE
                    binding.searchViewRecyclerView.visibility = View.VISIBLE
                    it.data?.let { list ->
                        youtubeContentViewRecyclerViewAdapter.setYoutubeContentViewList(list)
                    }
                    binding.searchViewRecyclerView.adapter = youtubeContentViewRecyclerViewAdapter
                }

                DataStatus.Status.EmptyResult -> {
                    binding.searchViewRecyclerView.visibility = View.GONE
                    binding.searchViewNoSearchResultConstraintLayout.visibility = View.VISIBLE
                }

                else -> {

                }
            }
        }

        viewModel.viewPagerBannerAds.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    it.data?.let { banners ->
                        binding.appBarLayout.visibility = View.VISIBLE
                        binding.infiniteViewPager.setBackgroundColor(requireContext().getColor(R.color.black))
                        infiniteRecyclerAdapter =
                            InfiniteViewPagerAdapter(banners, this@HomeFragment)
                        binding.infiniteViewPager.currentItem = 1
                        onInfinitePageChangeCallback(banners.size + 2)
                        startAutoScroll()
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.appBarLayout.visibility = View.GONE
                    loadingBannersFailed = true
                }

                DataStatus.Status.Loading -> {
                    binding.appBarLayout.visibility = View.VISIBLE
                    binding.infiniteViewPager.setBackgroundColor(requireContext().getColor(R.color.black_400))
                }

                else -> {
                    binding.appBarLayout.visibility = View.GONE
                }
            }
        }

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
                        viewModel.getHomePageBannerAds()
                    }
                }

                false -> {

                }
            }
        }

        binding.retryAfterNoResultButton.setOnClickListener {
            viewModel.getReels(selectedReelTopicId)
        }

        binding.retryAfterNoInternetButton.setOnClickListener {
            viewModel.getReels(selectedReelTopicId)
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
    }

    private fun startAutoScroll() {
        val runnable = object : Runnable {
            override fun run() {
                if (currentPage == (binding.infiniteViewPager.adapter?.itemCount ?: 1)) {
                    currentPage = 1
                }
                binding.infiniteViewPager.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, delay3k)
            }
        }
        handler.postDelayed(runnable, delay3k)
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
                    handler.postDelayed(this, delay3k)
                }
            }
            handler.postDelayed(hintRunnable!!, 0)
        }
    }

    private fun onInfinitePageChangeCallback(listSize: Int) {
        binding.infiniteViewPager.adapter = null
        binding.infiniteViewPager.adapter = infiniteRecyclerAdapter
        binding.infiniteViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (binding.infiniteViewPager.currentItem) {
                        listSize - 1 -> binding.infiniteViewPager.setCurrentItem(
                            1,
                            false
                        )

                        0 -> binding.infiniteViewPager.setCurrentItem(
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
            handler.removeCallbacksAndMessages(null)
        }
        hintRunnable = null
        hintHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (infiniteRecyclerAdapter != null) {
            handler.removeCallbacksAndMessages(null)
        }
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
        findNavController().navigate(R.id.discoverFragment)
    }

    override fun onResume() {
        super.onResume()
        if (selectedReelTopicPosition != -1 && selectedReelTopicId.isNotBlank()) {
            homeFragmentChipRecyclerViewAdapter.checkedPosition = selectedReelTopicPosition
            homeFragmentChipRecyclerViewAdapter.notifyItemChanged(selectedReelTopicPosition)
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
}