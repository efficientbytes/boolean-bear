package app.efficientbytes.booleanbear.ui.fragments

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentListReelsBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.ui.adapters.YoutubeContentViewRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.createShareIntent
import app.efficientbytes.booleanbear.utils.dummyReelsList
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.ListReelViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import java.util.Locale

class ListReelsFragment : Fragment(), YoutubeContentViewRecyclerViewAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentListReelsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: ListReelViewModel by viewModels()
    private lateinit var topicId: String
    private var topic: String = ""
    private var toolbar: MaterialToolbar? = null
    private val safeArgs: ListReelsFragmentArgs by navArgs()
    private val reelsRecyclerAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(
            dummyReelsList,
            requireContext(),
            this@ListReelsFragment
        )
    }
    private val searchResultRecyclerAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(
            dummyReelsList,
            requireContext(),
            this@ListReelsFragment
        )
    }
    private val connectivityListener: ConnectivityListener by inject()
    private var loadingReelsFailed = false
    private var loginToContinueFragment: LoginToContinueFragment? = null
    private val delay3k: Long = 3000 // Delay in milliseconds
    private var searchView: SearchView? = null
    private var isSearchViewOpen = false
    private val alternateSearchHint = "Search for tags \"#advanced\""
    private val hintHandler = Handler(Looper.getMainLooper())
    private var isFirstHintText = false
    private var hintRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListReelsBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.topicId = safeArgs.topicId
        viewModel.getTopicDetail(topicId)
        viewModel.getReels(topicId)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.list_reels_toolbar, menu)
                val search = menu.findItem(R.id.listReelsSearchMenu)
                search.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                        requireActivity().invalidateOptionsMenu()
                        isSearchViewOpen = false
                        hintHandler.removeCallbacksAndMessages(null)
                        hintRunnable = null
                        binding.searchViewParentConstraintLayout.visibility = View.GONE
                        binding.reelsViewParentConstraintLayout.visibility = View.VISIBLE
                        hideSearchResultView()
                        return true
                    }

                })
                searchView = search.actionView as? SearchView
                searchView?.queryHint = "Search..."
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
                    menu.findItem(R.id.listReelsShareMenu).setVisible(false)
                    isSearchViewOpen = true
                    startAlternateHint()
                    binding.reelsViewParentConstraintLayout.visibility = View.GONE
                    binding.searchViewParentConstraintLayout.visibility = View.VISIBLE
                    binding.searchViewNoSearchResultLinearLayout.visibility = View.GONE
                    binding.searchViewRecyclerView.visibility = View.GONE
                    viewModel.getReelQueries(topicId)
                }
                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.apply {
                            viewModel.getReelQueries(topicId, this)
                        }
                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        query?.apply {
                            if (isSearchViewOpen) {
                                if (!query.startsWith("#")) {
                                    viewModel.getReelQueries(topicId, this)
                                } else {
                                    binding.searchViewRecyclerView.visibility = View.GONE
                                    binding.searchViewNoSearchResultLinearLayout.visibility =
                                        View.GONE
                                }
                            }
                        }
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.listReelsSearchMenu -> {
                        return true
                    }

                    R.id.listReelsShareMenu -> {
                        if (topic.isNotEmpty()) {
                            val shareLink =
                                "https://app.booleanbear.com/binge-watch/topic/${topicId}"
                            val message =
                                "Experience the best of $topic learning with Boolean Bear, the Android-exclusive app! \uD83D\uDE80 \n"
                            val shareIntent = createShareIntent(shareLink, message)
                            startActivity(shareIntent)
                        }
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
        val activity = requireActivity()

        if (toolbar == null) {
            toolbar = activity.findViewById(R.id.mainToolbar)
            toolbar?.setTitleTextAppearance(
                requireContext(),
                R.style.ListReelsToolbarTitleAppearance
            )
            toolbar?.title = "Let's binge watch"
            toolbar?.setSubtitleTextAppearance(
                requireContext(),
                R.style.ListReelsToolbarSubTitleAppearance
            )
        }

        binding.searchViewParentConstraintLayout.visibility = View.GONE
        binding.reelsViewParentConstraintLayout.visibility = View.VISIBLE
        binding.reelsRecyclerView.visibility = View.VISIBLE
        //reels
        binding.reelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reelsRecyclerView.adapter = reelsRecyclerAdapter
        viewModel.reels.observe(viewLifecycleOwner) {
            if (!isSearchViewOpen) binding.reelsViewParentConstraintLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    toolbar?.subtitle = "Failed to fetch contents..."
                    reelsLoadingFailed()
                }

                DataStatus.Status.Loading -> {
                    toolbar?.subtitle = "Loading..."
                    reelsLoading()
                }

                DataStatus.Status.Success -> {
                    it.data?.let { list ->
                        reelsLoaded()
                        toolbar?.subtitle = "${list.size} contents"
                        binding.reelsRecyclerView.adapter = reelsRecyclerAdapter
                        reelsRecyclerAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.EmptyResult -> {
                    toolbar?.subtitle = "Currently no contents available..."
                    emptyReels()
                }

                DataStatus.Status.NoInternet -> {
                    toolbar?.subtitle = "No internet!"
                    loadingReelsFailed = true
                    reelsLoadingFailed()
                }

                DataStatus.Status.TimeOut -> {
                    toolbar?.subtitle = "Time out. Try again"
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
            viewModel.getTopicDetail(topicId)
            viewModel.getReels(topicId)
        }

        viewModel.topicResult.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    toolbar?.title = "Let's binge watch"
                }

                DataStatus.Status.NoInternet, DataStatus.Status.TimeOut -> {
                    loadingReelsFailed = true
                    toolbar?.title = "Let's binge watch"
                }

                DataStatus.Status.Success -> {
                    it.data?.let { remoteReelTopic ->
                        this@ListReelsFragment.topic = remoteReelTopic.topic
                        toolbar?.title = this@ListReelsFragment.topic
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    findNavController().popBackStack()
                }
            }
        }
        //search
        binding.searchViewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchViewRecyclerView.adapter = searchResultRecyclerAdapter

        viewModel.searchResult.observe(viewLifecycleOwner) {
            if (isSearchViewOpen) binding.searchViewParentConstraintLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Loading -> {
                    searchResultSuccess()
                    searchResultRecyclerAdapter.setYoutubeContentViewList(dummyReelsList)
                }

                DataStatus.Status.Success -> {
                    it.data?.let { list ->
                        searchResultSuccess()
                        searchResultRecyclerAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.EmptyResult -> {
                    noSearchResult()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    noSearchResult()
                }
            }
        }
        //connectivity listener
        connectivityListener.observe(viewLifecycleOwner) { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (loadingReelsFailed) {
                        loadingReelsFailed = false
                        viewModel.getTopicDetail(topicId)
                        viewModel.getReels(topicId)
                    }
                }

                false -> {

                }
            }
        }
    }

    private fun hideSearchResultView() {
        binding.searchViewNoSearchResultLinearLayout.visibility = View.GONE
        binding.searchViewRecyclerView.visibility = View.GONE
    }

    private fun emptyReels() {
        binding.reelsRecyclerView.adapter = null
        binding.reelsRecyclerView.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.noResultLinearLayout.visibility = View.VISIBLE
    }

    private fun reelsLoading() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.VISIBLE
        binding.reelsRecyclerView.adapter = null
        reelsRecyclerAdapter.setYoutubeContentViewList(dummyReelsList)
        binding.reelsRecyclerView.adapter = reelsRecyclerAdapter
    }

    private fun reelsLoaded() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.VISIBLE
    }

    private fun reelsLoadingFailed() {
        binding.noResultLinearLayout.visibility = View.GONE
        binding.reelsRecyclerView.visibility = View.GONE
        binding.reelsRefreshButton.visibility = View.VISIBLE
    }

    private fun noSearchResult() {
        binding.searchViewRecyclerView.visibility = View.GONE
        binding.searchViewNoSearchResultLinearLayout.visibility = View.VISIBLE
    }

    private fun searchResultSuccess() {
        binding.searchViewNoSearchResultLinearLayout.visibility = View.GONE
        binding.searchViewRecyclerView.visibility = View.VISIBLE
    }

    private fun startAlternateHint() {
        if (hintRunnable == null) {
            hintRunnable = object : Runnable {
                override fun run() {
                    if (isFirstHintText) {
                        searchView?.queryHint = alternateSearchHint
                    } else {
                        searchView?.queryHint =
                            "Search for ${topic.lowercase(Locale.ROOT)} contents"
                    }
                    isFirstHintText = !isFirstHintText
                    hintHandler.postDelayed(this, delay3k)
                }
            }
            hintHandler.postDelayed(hintRunnable!!, 0)
        }
    }

    override fun onYoutubeContentViewItemClicked(position: Int, remoteReel: RemoteReel) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val directions =
                ListReelsFragmentDirections.listReelsFragmentToShuffledContentPlayerFragment(reelId = remoteReel.reelId)
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

    override fun onResume() {
        super.onResume()
        if (isSearchViewOpen) {
            startAlternateHint()
        }
        if (toolbar == null) {
            toolbar = requireActivity().findViewById(R.id.mainToolbar)
        }
    }

}