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
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.databinding.FragmentHomeBinding
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.services.models.RemoteShuffledContent
import app.efficientbytes.booleanbear.ui.adapters.HomeFragmentChipRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.InfiniteViewPagerAdapter
import app.efficientbytes.booleanbear.ui.adapters.YoutubeContentViewRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.viewmodels.HomeViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var homeFragmentChipRecyclerViewAdapter: HomeFragmentChipRecyclerViewAdapter
    private lateinit var youtubeContentViewRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter
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

        var selectedCategoryId = ""
        var selectedCategoryPosition = -1
        var selectedCategoryTitle = ""
        var loadingCategoriesFailed = false
        var loadingContentsFailed = false
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
                searchView?.setOnCloseListener {
                    isSearchViewOpen = false
                    hintHandler.removeCallbacksAndMessages(null)
                    binding.searchConstraintParentLayout.visibility = View.GONE
                    binding.searchViewRecyclerView.adapter = null
                    binding.appBarLayout.visibility = View.VISIBLE
                    binding.contentParentConstraintLayout.visibility = View.VISIBLE
                    false
                }
                searchView?.setOnSearchClickListener {
                    isSearchViewOpen = true
                    startAlternateHint()
                    binding.appBarLayout.visibility = View.GONE
                    binding.contentParentConstraintLayout.visibility = View.GONE
                    binding.searchConstraintParentLayout.visibility = View.VISIBLE
                    viewModel.getSearchContents(selectedCategoryId)
                }
                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.apply {
                            viewModel.getSearchContents(selectedCategoryId, this)
                        }
                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        query?.apply {
                            viewModel.getSearchContents(selectedCategoryId, this)
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

                    R.id.searchMenu -> {

                    }
                }
                return false
            }
        }, viewLifecycleOwner)
        //set content category recycler view
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        homeFragmentChipRecyclerViewAdapter =
            HomeFragmentChipRecyclerViewAdapter(emptyList(), requireContext(), this)
        binding.categoriesRecyclerView.adapter = homeFragmentChipRecyclerViewAdapter

        viewModel.contentCategoriesFromDB.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.contentCategoryScrollView.visibility = View.GONE
                binding.contentCategoryShimmerLinearLayout.visibility = View.GONE
                binding.categoriesRecyclerView.visibility = View.VISIBLE
                homeFragmentChipRecyclerViewAdapter.setContentCategories(it.toList().subList(0, 6))
                selectedCategoryTitle = it.toList()[0].title
            } else {
                binding.contentCategoryScrollView.visibility = View.VISIBLE
                binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                binding.categoriesRecyclerView.visibility = View.INVISIBLE
                searchView?.apply {
                    visibility = View.GONE
                }
            }
        }
        //set search recycler view
        binding.searchConstraintParentLayout.visibility = View.GONE
        binding.searchViewRecyclerView.adapter = null
        binding.searchViewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerViewAdapter =
            YoutubeContentViewRecyclerViewAdapter(emptyList(), requireContext(), this@HomeFragment)
        viewModel.searchResult.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    binding.searchViewNoSearchResultConstraintLayout.visibility = View.GONE
                    binding.searchViewRecyclerView.visibility = View.VISIBLE
                    it.data?.let { list ->
                        youtubeContentViewRecyclerViewAdapter =
                            YoutubeContentViewRecyclerViewAdapter(
                                list,
                                requireContext(),
                                this@HomeFragment
                            )
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
        //set contents recycler view
        binding.contentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.remoteShuffledContentList.observe(viewLifecycleOwner) {
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
                        youtubeContentViewRecyclerViewAdapter =
                            YoutubeContentViewRecyclerViewAdapter(
                                list,
                                requireContext(),
                                this@HomeFragment
                            )
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
                    loadingContentsFailed = true
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

        viewModel.categories.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.EmptyResult -> {
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
                }

                DataStatus.Status.Failed -> {
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
                }

                DataStatus.Status.Loading -> {
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
                }

                DataStatus.Status.NoInternet -> {
                    loadingCategoriesFailed = true
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
                }

                DataStatus.Status.Success -> {
                    binding.contentCategoryScrollView.visibility = View.GONE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.GONE
                    binding.categoriesRecyclerView.visibility = View.VISIBLE
                }

                DataStatus.Status.TimeOut -> {
                    loadingContentsFailed = true
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
                }

                DataStatus.Status.UnKnownException -> {
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
                }

                else -> {
                    binding.contentCategoryScrollView.visibility = View.VISIBLE
                    binding.contentCategoryShimmerLinearLayout.visibility = View.VISIBLE
                    binding.categoriesRecyclerView.visibility = View.GONE
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
                    loadingContentsFailed = true
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
                    if (loadingCategoriesFailed) {
                        loadingCategoriesFailed = false
                        viewModel.getShuffledCategories()
                    }
                    if (loadingContentsFailed) {
                        loadingContentsFailed = false
                        viewModel.getYoutubeViewContentsUnderShuffledCategory(selectedCategoryId)
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
            viewModel.getYoutubeViewContentsUnderShuffledCategory(selectedCategoryId)
        }

        binding.retryAfterNoInternetButton.setOnClickListener {
            viewModel.getYoutubeViewContentsUnderShuffledCategory(selectedCategoryId)
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
                            "Search for ${selectedCategoryTitle.lowercase(Locale.ROOT)} contents"
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

    override fun onChipItemClicked(position: Int, shuffledCategory: ShuffledCategory) {
        selectedCategoryPosition = position
        selectedCategoryId = shuffledCategory.id
        viewModel.getYoutubeViewContentsUnderShuffledCategory(selectedCategoryId)
        val title = shuffledCategory.title
        selectedCategoryTitle = title
    }

    override fun onChipLastItemClicked() {
        val snackBar = Snackbar.make(
            binding.coordinatorLayout,
            "Feature is still under development.",
            Snackbar.LENGTH_LONG
        )
        snackBar.show()
    }

    override fun onResume() {
        super.onResume()
        if (selectedCategoryPosition != -1 && selectedCategoryId.isBlank()) {
            homeFragmentChipRecyclerViewAdapter.checkedPosition = selectedCategoryPosition
            homeFragmentChipRecyclerViewAdapter.notifyItemChanged(selectedCategoryPosition)
        }
        if (isSearchViewOpen) {
            startAlternateHint()
        }
    }

    override fun onYoutubeContentViewItemClicked(
        position: Int,
        remoteShuffledContent: RemoteShuffledContent
    ) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val directions =
                HomeFragmentDirections.homeFragmentToShuffledContentPlayerFragment(
                    remoteShuffledContent.contentId
                )
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
                HomeFragmentDirections.homeFragmentToShuffledContentPlayerFragment(
                    contentId
                )
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