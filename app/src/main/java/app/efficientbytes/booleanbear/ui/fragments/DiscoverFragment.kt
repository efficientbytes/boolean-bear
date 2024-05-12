package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import app.efficientbytes.booleanbear.databinding.FragmentDiscoverBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import app.efficientbytes.booleanbear.ui.adapters.CourseBundleRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.ReelTopicsRecyclerViewAdapter
import app.efficientbytes.booleanbear.utils.CustomLinearLayoutManager
import app.efficientbytes.booleanbear.utils.dummyCourseBundle
import app.efficientbytes.booleanbear.utils.dummyReelTopicsList
import app.efficientbytes.booleanbear.viewmodels.DiscoverViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class DiscoverFragment : Fragment(), ReelTopicsRecyclerViewAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentDiscoverBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val reelTopicsRecyclerViewAdapter: ReelTopicsRecyclerViewAdapter by lazy {
        ReelTopicsRecyclerViewAdapter(dummyReelTopicsList, requireContext(), this@DiscoverFragment)
    }
    private val courseBundlesRecyclerViewAdapter: CourseBundleRecyclerViewAdapter by lazy {
        CourseBundleRecyclerViewAdapter(dummyCourseBundle, requireContext())
    }
    private val viewModel: DiscoverViewModel by inject()

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

        binding.refreshButton.visibility = View.GONE
        val reelTopicsManager = GridLayoutManager(context, 2)
        binding.reelTopicsRecyclerView.layoutManager = reelTopicsManager
        binding.reelTopicsRecyclerView.adapter = reelTopicsRecyclerViewAdapter

        viewModel.reelTopics.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    binding.reelTopicsRecyclerView.visibility = View.VISIBLE
                    binding.refreshButton.visibility = View.GONE
                    reelTopicsRecyclerViewAdapter.setReelTopicList(dummyReelTopicsList)
                }

                DataStatus.Status.Success -> {
                    binding.reelTopicsRecyclerView.visibility = View.VISIBLE
                    binding.refreshButton.visibility = View.GONE
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

                DataStatus.Status.TimeOut -> {
                    binding.reelTopicsRecyclerView.visibility = View.INVISIBLE
                    binding.refreshButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Failed -> {
                    binding.reelTopicsRecyclerView.visibility = View.INVISIBLE
                    binding.refreshButton.visibility = View.VISIBLE
                }

                DataStatus.Status.NoInternet -> {
                    binding.reelTopicsRecyclerView.visibility = View.INVISIBLE
                    binding.refreshButton.visibility = View.VISIBLE
                }

                else -> {
                    it.message?.let { message ->
                        Snackbar.make(
                            binding.parentScrollView,
                            message,
                            Snackbar.LENGTH_INDEFINITE
                        ).show()
                    }
                    binding.reelTopicsRecyclerView.visibility = View.INVISIBLE
                    binding.refreshButton.visibility = View.VISIBLE
                }
            }
        }

        binding.refreshButton.setOnClickListener {
            viewModel.getReelTopics()
        }
        val linearLayoutManager = CustomLinearLayoutManager(requireContext())
        linearLayoutManager.setScrollEnabled(false)
        binding.courseBundleRecyclerView.layoutManager = linearLayoutManager

        binding.courseBundleRecyclerView.adapter = courseBundlesRecyclerViewAdapter

        viewModel.courseBundle.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.EmptyResult -> {

                }

                DataStatus.Status.Failed -> {

                }

                DataStatus.Status.Loading -> {
                    courseBundlesRecyclerViewAdapter.setCourseTopicList(dummyCourseBundle)
                }

                DataStatus.Status.NoInternet -> {

                }

                DataStatus.Status.Success -> {
                    it.data?.let { list ->
                        courseBundlesRecyclerViewAdapter.setCourseTopicList(list)
                    }
                }

                else -> {

                }
            }
        }


    }

    override fun onReelTopicItemClicked(remoteReelTopic: RemoteReelTopic) {
        val directions =
            DiscoverFragmentDirections.discoverFragmentToListReelsFragment(
                remoteReelTopic.topicId,
                remoteReelTopic.topic
            )
        findNavController().navigate(directions)
    }

}