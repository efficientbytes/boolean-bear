package app.efficientbytes.booleanbear.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentContactUsBinding
import app.efficientbytes.booleanbear.utils.AppAuthStateListener
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class ContactUsFragment : Fragment() {

    private lateinit var _binding: FragmentContactUsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val appAuthStateListener: AppAuthStateListener by inject()
    private val reviewManager: ReviewManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.customAuthState = appAuthStateListener
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val versionName = "version " + getVersionCode()
        binding.appVersionLabelTextView.text = versionName

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
            if (FirebaseAuth.getInstance().currentUser != null) {
                findNavController().navigate(R.id.contactUsFragment_to_shareFeedbackFragment)
            } else {
                Toast.makeText(
                    requireActivity(),
                    "You need to sign in to share your feedback.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.contactSupportLabelTextView.setOnClickListener {
            findNavController().navigate(R.id.contactUsFragment_to_describeIssueFragment)
        }

        binding.deleteAccountLabelTextView.setOnClickListener {
            findNavController().navigate(R.id.contactUsFragment_to_deleteUserAccountFragment)
        }

        binding.privacyPolicyLabelTextView.setOnClickListener {
            val link =
                "https://efficientbytes.notion.site/Boolean-Bear-Privacy-Policy-b2f43ae39b8a4c5880ef2a1cbd811b15"
            openLink(link)
        }

        binding.termsNConditionLabelTextView.setOnClickListener {
            val link =
                "https://efficientbytes.notion.site/Boolean-Bear-Terms-of-use-803e636c627946e4b6fdefdbf23b9ede"
            openLink(link)
        }

    }

    private fun openLink(link: String) {
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireContext().startActivity(intent)
    }

    private fun getVersionCode(): String {
        var version: String = ""
        activity?.let {
            version = it.packageManager.getPackageInfo(it.packageName, 0).versionName
        }
        return version
    }

}