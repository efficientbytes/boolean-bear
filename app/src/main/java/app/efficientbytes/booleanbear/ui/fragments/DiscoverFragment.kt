package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import app.efficientbytes.booleanbear.databinding.FragmentDiscoverBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteCourse
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import app.efficientbytes.booleanbear.ui.adapters.CourseBundleRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.CourseRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.ReelTopicsRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomLinearLayoutManager
import app.efficientbytes.booleanbear.utils.Pi.dummyCourseBundle
import app.efficientbytes.booleanbear.utils.Pi.dummyReelTopicsList
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.DiscoverViewModel
import org.koin.android.ext.android.inject

class DiscoverFragment : Fragment(), ReelTopicsRecyclerViewAdapter.OnItemClickListener,
    CourseRecyclerViewAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentDiscoverBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val reelTopicsRecyclerViewAdapter: ReelTopicsRecyclerViewAdapter by lazy {
        ReelTopicsRecyclerViewAdapter(dummyReelTopicsList, requireContext(), this@DiscoverFragment)
    }
    private val courseBundlesRecyclerViewAdapter: CourseBundleRecyclerViewAdapter by lazy {
        CourseBundleRecyclerViewAdapter(dummyCourseBundle, requireContext(), this@DiscoverFragment)
    }
    private val viewModel: DiscoverViewModel by viewModels()
    private val connectivityListener: ConnectivityListener by inject()
    private var reelTopicsInternetIssue = false
    private var courseBundleInternetIssue = false
    private var courseWaitingListFragment: CourseWaitingListFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycle.addObserver(viewModel)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshReelTopicButton.visibility = View.GONE
        val reelTopicsManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
        binding.reelTopicsRecyclerView.layoutManager = reelTopicsManager
        binding.reelTopicsRecyclerView.adapter = reelTopicsRecyclerViewAdapter

        viewModel.reelTopics.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    hideReelTopicsRefreshButton()
                    reelTopicsRecyclerViewAdapter.setReelTopicList(dummyReelTopicsList)
                }

                DataStatus.Status.Success -> {
                    hideReelTopicsRefreshButton()
                    it.data?.let { list ->
                        reelTopicsRecyclerViewAdapter.setReelTopicList(list)
                        reelTopicsManager.spanSizeLookup =
                            object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    val totalItems = list.size
                                    val spanCount = reelTopicsManager.spanCount
                                    return if (position == totalItems - 1 && totalItems % 2 != 0) {
                                        spanCount
                                    } else {
                                        1
                                    }
                                }
                            }
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                DataStatus.Status.NoInternet -> {
                    reelTopicsInternetIssue = true
                    failedToLoadReelTopics()
                }

                else -> {
                    failedToLoadReelTopics()
                }
            }
        }

        binding.refreshReelTopicButton.setOnClickListener {
            viewModel.getReelTopics()
        }
        val linearLayoutManager = CustomLinearLayoutManager(requireContext())
        linearLayoutManager.setScrollEnabled(false)
        binding.courseBundleRecyclerView.layoutManager = linearLayoutManager

        binding.courseBundleRecyclerView.adapter = courseBundlesRecyclerViewAdapter

        viewModel.courseBundle.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    hideCourseBundleRefreshButton()
                    courseBundlesRecyclerViewAdapter.setCourseTopicList(dummyCourseBundle)
                }

                DataStatus.Status.Success -> {
                    it.data?.let { list ->
                        hideCourseBundleRefreshButton()
                        courseBundlesRecyclerViewAdapter.setCourseTopicList(list)
                    }
                }

                DataStatus.Status.NoInternet -> {
                    courseBundleInternetIssue = true
                    failedToLoadCourseBundle()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    failedToLoadCourseBundle()
                }
            }
        }

        binding.refreshCourseTopicButton.setOnClickListener {
            viewModel.getCourseBundle()
        }
        //connectivity listener
        connectivityListener.observe(viewLifecycleOwner) { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (reelTopicsInternetIssue) {
                        reelTopicsInternetIssue = false
                        viewModel.getReelTopics()
                    }
                    if (courseBundleInternetIssue) {
                        courseBundleInternetIssue = false
                        viewModel.getCourseBundle()
                    }
                }

                false -> {

                }
            }
        }

    }

    private fun failedToLoadCourseBundle() {
        binding.courseBundleRecyclerView.visibility = View.INVISIBLE
        binding.refreshCourseTopicButton.visibility = View.VISIBLE
    }

    private fun failedToLoadReelTopics() {
        binding.reelTopicsRecyclerView.visibility = View.INVISIBLE
        binding.refreshReelTopicButton.visibility = View.VISIBLE
    }

    private fun hideReelTopicsRefreshButton() {
        binding.reelTopicsRecyclerView.visibility = View.VISIBLE
        binding.refreshReelTopicButton.visibility = View.GONE
    }

    private fun hideCourseBundleRefreshButton() {
        binding.courseBundleRecyclerView.visibility = View.VISIBLE
        binding.refreshCourseTopicButton.visibility = View.GONE
    }

    override fun onReelTopicItemClicked(remoteReelTopic: RemoteReelTopic) {
        val directions =
            DiscoverFragmentDirections.discoverFragmentToListReelsFragment(remoteReelTopic.topicId)
        findNavController().navigate(directions)
    }

    override fun onCourseItemClicked(remoteCourse: RemoteCourse) {
        if (!remoteCourse.isAvailable) {
            if (courseWaitingListFragment == null) {
                courseWaitingListFragment = CourseWaitingListFragment()
            }
            if (!CourseWaitingListFragment.isOpened) {
                CourseWaitingListFragment.isOpened = true
                CourseWaitingListFragment.courseId = remoteCourse.courseId
                CourseWaitingListFragment.nonAvailabilityReason = remoteCourse.nonAvailabilityReason
                courseWaitingListFragment!!.show(
                    parentFragmentManager,
                    CourseWaitingListFragment.COURSE_WAITING_LIST_FRAGMENT
                )
            }
        }
    }

}