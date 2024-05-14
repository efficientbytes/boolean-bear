package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.efficientbytes.booleanbear.databinding.FragmentCourseWaitingListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CourseWaitingListFragment : BottomSheetDialogFragment() {

    companion object {

        const val COURSE_WAITING_LIST_FRAGMENT: String = "frag-course-wait-list"
        var isOpened: Boolean = false
    }

    private lateinit var _binding: FragmentCourseWaitingListBinding
    private val binding get() = _binding
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseWaitingListBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joinTheWaitingListButton.setOnClickListener {
        }

    }

}