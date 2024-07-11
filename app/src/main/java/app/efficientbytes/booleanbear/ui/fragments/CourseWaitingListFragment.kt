package app.efficientbytes.booleanbear.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentCourseWaitingListBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.CourseWaitingListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class CourseWaitingListFragment : BottomSheetDialogFragment() {

    companion object {

        const val COURSE_WAITING_LIST_FRAGMENT: String = "frag-course-wait-list"
        var isOpened: Boolean = false
        var courseId: String? = null
        var nonAvailabilityReason: String? = null
        var isRegisteredUser: Boolean? = false
    }

    private lateinit var _binding: FragmentCourseWaitingListBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: CourseWaitingListViewModel by viewModels()

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

        isRegisteredUser = courseId?.let { viewModel.hasUserJoinedWaitList(it) }

        binding.joinTheWaitingListButton.apply {
            isRegisteredUser?.let { registered ->
                text = if (registered) {
                    "You have joined waiting list already"
                } else {
                    "Join The Waiting List"
                }
                isEnabled = !registered
                if (isEnabled) {
                    setBackgroundColor(requireContext().getColor(R.color.md_theme_onPrimary))
                    setBackgroundColor(requireContext().getColor(R.color.violet_300))
                } else {
                    setBackgroundColor(requireContext().getColor(R.color.black_300))
                    setTextColor(requireContext().getColor(R.color.black))
                }
            }
            setOnClickListener {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    courseId?.let { id ->
                        isEnabled = false
                        viewModel.joinCourseWaitingList(id)
                    }
                } else {
                    Snackbar.make(
                        binding.parentLinearLayout,
                        "Please Sign up to join our waiting list.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        nonAvailabilityReason?.let { message ->
            binding.additionalMessageLabelTextView.text = message
        }

        viewModel.courseWaitingList.observe(viewLifecycleOwner) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.Failed -> {
                        binding.joinTheWaitingListButton.isEnabled = true
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.text = it.message
                        viewModel.resetWaitingList()
                    }

                    DataStatus.Status.Loading -> {
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = getString(R.string.please_wait)
                        viewModel.resetWaitingList()
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.joinTheWaitingListButton.isEnabled = true
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.no_internet_connection_please_try_again)
                        viewModel.resetWaitingList()
                    }

                    DataStatus.Status.Success -> {
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.text = it.message
                        binding.joinTheWaitingListButton.isEnabled = false
                        binding.joinTheWaitingListButton.setBackgroundColor(
                            requireContext().getColor(
                                R.color.black_300
                            )
                        )
                        binding.joinTheWaitingListButton.setTextColor(requireContext().getColor(R.color.black))
                        viewModel.resetWaitingList()
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.joinTheWaitingListButton.isEnabled = true
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.time_out_please_try_again)
                        viewModel.resetWaitingList()
                    }

                    DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                        requireContext(),
                        it.message
                    )

                    else -> {
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                        viewModel.resetWaitingList()
                    }
                }
            }
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isOpened = false
        courseId = null
        nonAvailabilityReason = null
        isRegisteredUser = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpened = false
        courseId = null
        nonAvailabilityReason = null
        isRegisteredUser = false
    }

    override fun onDetach() {
        super.onDetach()
        isOpened = false
        courseId = null
        nonAvailabilityReason = null
        isRegisteredUser = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOpened = false
        courseId = null
        nonAvailabilityReason = null
        isRegisteredUser = false
    }

}