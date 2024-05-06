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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentListReelsBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.ui.adapters.YoutubeContentViewRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.viewmodels.ListReelViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import java.util.Locale

class ListReelsFragment : Fragment(), YoutubeContentViewRecyclerViewAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentListReelsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: ListReelViewModel by inject()
    private lateinit var topicId: String
    private lateinit var topic: String
    private val youtubeContentViewRecyclerViewAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(emptyList(), requireContext(), this@ListReelsFragment)
    }
    private val searchResultAdapter: YoutubeContentViewRecyclerViewAdapter by lazy {
        YoutubeContentViewRecyclerViewAdapter(emptyList(), requireContext(), this@ListReelsFragment)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = ListReelsFragmentArgs.fromBundle(bundle)
        this.topicId = args.topicId
        this.topic = args.topic
        viewModel.getReels(topicId)
    }

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
                        hintRunnable = null
                        hintHandler.removeCallbacksAndMessages(null)
                        isSearchViewOpen = false
                        binding.searchViewNoSearchResultLinearLayout.visibility = View.GONE
                        binding.searchViewRecyclerView.visibility = View.GONE
                        binding.searchViewParentConstraintLayout.visibility = View.GONE
                        binding.reelsViewParentConstraintLayout.visibility = View.VISIBLE
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
                    viewModel.getReelQueries(topicId)
                    binding.reelsViewParentConstraintLayout.visibility = View.GONE
                    binding.searchViewParentConstraintLayout.visibility = View.VISIBLE
                    binding.searchViewNoSearchResultLinearLayout.visibility = View.GONE
                    binding.searchViewRecyclerView.visibility = View.VISIBLE
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
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
        val activity = requireActivity()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.mainToolbar)
        toolbar.setTitleTextAppearance(requireContext(), R.style.ListReelsToolbarTitleAppearance)
        toolbar.title = this.topic
        toolbar.setSubtitleTextAppearance(
            requireContext(),
            R.style.ListReelsToolbarSubTitleAppearance
        )

        binding.reelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reelsRecyclerView.adapter = youtubeContentViewRecyclerViewAdapter

        viewModel.reels.observe(viewLifecycleOwner) {
            binding.searchViewParentConstraintLayout.visibility = View.GONE
            binding.reelsViewParentConstraintLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    toolbar.subtitle = "Failed to fetch contents..."
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.noSearchResultLinearLayout.visibility = View.GONE
                    binding.reelsRecyclerView.visibility = View.GONE
                }

                DataStatus.Status.Loading -> {
                    toolbar.subtitle = "Loading..."
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.reelsRecyclerView.visibility = View.GONE
                    binding.noSearchResultLinearLayout.visibility = View.GONE
                    binding.shimmerLayoutNestedScrollView.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                }

                DataStatus.Status.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.noSearchResultLinearLayout.visibility = View.GONE
                    binding.reelsRecyclerView.visibility = View.VISIBLE
                    it.data?.let { list ->
                        toolbar.subtitle = "${list.size} contents"
                        youtubeContentViewRecyclerViewAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.EmptyResult -> {
                    toolbar.subtitle = "Currently no contents available..."
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.reelsRecyclerView.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noSearchResultLinearLayout.visibility = View.VISIBLE
                }

                DataStatus.Status.NoInternet -> {
                    toolbar.subtitle = "No internet!"
                    loadingReelsFailed = true
                    binding.reelsRecyclerView.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.noSearchResultLinearLayout.visibility = View.GONE
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.VISIBLE
                }

                DataStatus.Status.TimeOut -> {
                    toolbar.subtitle = "Oops! Time out. Try again"
                }

                DataStatus.Status.UnKnownException -> {
                    toolbar.subtitle = "Failed to fetch contents..."
                    it.message?.let { message ->
                        val snackBar = Snackbar.make(
                            binding.parentConstraintLayout,
                            message,
                            Snackbar.LENGTH_LONG
                        )
                        snackBar.show()
                    }
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.noSearchResultLinearLayout.visibility = View.GONE
                    binding.noInternetLinearLayout.visibility = View.GONE
                    binding.reelsRecyclerView.visibility = View.GONE
                }

                else -> {

                }
            }
        }

        binding.searchViewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchViewRecyclerView.adapter = searchResultAdapter

        viewModel.searchResult.observe(viewLifecycleOwner) {
            binding.reelsViewParentConstraintLayout.visibility = View.GONE
            binding.searchViewParentConstraintLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Success -> {
                    binding.searchViewNoSearchResultLinearLayout.visibility = View.GONE
                    binding.searchViewRecyclerView.visibility = View.VISIBLE
                    it.data?.let { list ->
                        searchResultAdapter.setYoutubeContentViewList(list)
                    }
                }

                DataStatus.Status.EmptyResult -> {
                    binding.searchViewRecyclerView.visibility = View.GONE
                    binding.searchViewNoSearchResultLinearLayout.visibility = View.VISIBLE
                }

                else -> {

                }
            }
        }

        binding.retryAfterNoInternetButton.setOnClickListener {
            viewModel.getReels(topicId)
        }

        binding.retryAfterNoResultButton.setOnClickListener {
            viewModel.getReels(topicId)
        }

        connectivityListener.observe(viewLifecycleOwner) { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (loadingReelsFailed) {
                        loadingReelsFailed = false
                        viewModel.getReels(topicId)
                    }
                }

                false -> {

                }
            }
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
    }

}