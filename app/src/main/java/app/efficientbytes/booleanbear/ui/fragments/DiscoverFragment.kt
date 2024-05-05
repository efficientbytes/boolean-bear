package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import app.efficientbytes.booleanbear.databinding.FragmentDiscoverBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import app.efficientbytes.booleanbear.ui.adapters.ReelTopicsRecyclerViewAdapter
import app.efficientbytes.booleanbear.viewmodels.DiscoverViewModel
import org.koin.android.ext.android.inject

class DiscoverFragment : Fragment(), ReelTopicsRecyclerViewAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentDiscoverBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val reelTopicsRecyclerViewAdapter: ReelTopicsRecyclerViewAdapter by lazy {
        ReelTopicsRecyclerViewAdapter(emptyList(), requireContext(), this@DiscoverFragment)
    }
    private val discoverViewModel: DiscoverViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycle.addObserver(discoverViewModel)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reelTopicsManager = GridLayoutManager(context, 2)
        binding.reelTopicsRecyclerView.layoutManager = reelTopicsManager

        discoverViewModel.reelTopics.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Success -> {
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
                        binding.reelTopicsRecyclerView.adapter = reelTopicsRecyclerViewAdapter
                    }
                }

                else -> {

                }
            }
        }


    }

    override fun onReelTopicItemClicked(remoteReelTopic: RemoteReelTopic) {

    }

}