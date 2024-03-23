package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentContactUsBinding
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.model.ReviewErrorCode
import org.koin.android.ext.android.inject

class ContactUsFragment : Fragment() {

    private val tagContactUFragment = "Contact-Us-Fragment"
    private lateinit var _binding: FragmentContactUsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
    private val reviewManager: ReviewManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rateUsLabelTextView.setOnClickListener {
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                    flow.addOnCompleteListener { _ ->
                    }
                } else {
                    @ReviewErrorCode val reviewErrorCode =
                        (task.exception as ReviewException).errorCode
                }
            }
        }

        binding.shareFeedbackLabelTextView.setOnClickListener {
            findNavController().navigate(R.id.contactUsFragment_to_shareFeedbackFragment)
        }

        binding.contactSupportLabelTextView.setOnClickListener{
            findNavController().navigate(R.id.contactUsFragment_to_describeIssueFragment)
        }

    }

}