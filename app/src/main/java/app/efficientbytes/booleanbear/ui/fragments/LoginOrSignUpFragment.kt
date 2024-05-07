package app.efficientbytes.booleanbear.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentLoginOrSignUpBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.validatePhoneNumberFormat
import app.efficientbytes.booleanbear.viewmodels.LoginOrSignUpViewModel
import org.koin.android.ext.android.inject

class LoginOrSignUpFragment : Fragment() {

    private lateinit var _binding: FragmentLoginOrSignUpBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: LoginOrSignUpViewModel by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginOrSignUpBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val policy = "By continuing, I agree to the Terms of Use & Privacy Policy"
        val termsOfUseIndex = 30
        val privacyPolicyIndex = 45
        val spannableStringBuilder = SpannableStringBuilder(policy)
        val termsOfUseClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val link =
                    "https://efficientbytes.notion.site/Boolean-Bear-Terms-of-use-803e636c627946e4b6fdefdbf23b9ede"
                openLink(link)
            }
        }
        spannableStringBuilder.setSpan(
            termsOfUseClickableSpan,
            termsOfUseIndex,
            42,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
            ForegroundColorSpan(requireContext().getColor(R.color.violet_800)),
            termsOfUseIndex,
            42,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val privacyPolicyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val link =
                    "https://efficientbytes.notion.site/Boolean-Bear-Privacy-Policy-b2f43ae39b8a4c5880ef2a1cbd811b15"
                openLink(link)
            }
        }
        spannableStringBuilder.setSpan(
            privacyPolicyClickableSpan,
            privacyPolicyIndex,
            59,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
            ForegroundColorSpan(requireContext().getColor(R.color.violet_800)),
            privacyPolicyIndex,
            59,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.policyNConditionsLabelTextView.text = spannableStringBuilder
        binding.policyNConditionsLabelTextView.movementMethod = LinkMovementMethod.getInstance()

        binding.continueButton.setOnClickListener {
            val input = binding.phoneNumberTextInputEditText.text.toString()
            if (validatePhoneNumberFormat(binding.phoneNumberTextInputLayout, input)) {
                viewModel.sendOTPToPhoneNumber(input)
            }
        }
        binding.phoneNumberTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.phoneNumberTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        viewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it.status) {
                    DataStatus.Status.Failed -> {
                        binding.continueButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = "${it.message}"
                    }

                    DataStatus.Status.Loading -> {
                        binding.continueButton.isEnabled = false
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            "Please wait while we send the OTP..."
                    }

                    DataStatus.Status.Success -> {
                        binding.continueButton.isEnabled = false
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = it.data?.message
                        binding.phoneNumberTextInputEditText.text = null
                        it.data?.phoneNumber?.also { phoneNumber ->
                            navigateToOTPVerificationPage(phoneNumber)
                            viewModel.resetLiveData()
                        }
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.continueButton.isEnabled = true
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            "No Internet Connection"
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.continueButton.isEnabled = true
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            "The process is taking unusually long time. Please try again"
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun navigateToOTPVerificationPage(phoneNumber: String) {
        val directions =
            LoginOrSignUpFragmentDirections.loginOrSignUpFragmentToOTPVerificationFragment(
                phoneNumber
            )
        rootView.findNavController().navigate(directions)
    }

    private fun openLink(link: String) {
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireContext().startActivity(intent)
    }

}