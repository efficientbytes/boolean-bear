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
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentLoginOrSignUpBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.LoginOrSignUpViewModel

class LoginOrSignUpFragment : Fragment() {

    private lateinit var _binding: FragmentLoginOrSignUpBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: LoginOrSignUpViewModel by viewModels()
    private var pageOpenedForFirstTime = true
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
        //terms and policy ui render
        val policy = getString(R.string.by_continuing_i_agree_to_the_terms_of_use_privacy_policy)
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

        binding.phoneNumberTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.phoneNumberTextInputEditText.text?.isEmpty() == true) {
                    binding.phoneNumberTextInputLayout.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumberTextInputEditText)

        binding.countryCodePicker.setPhoneNumberValidityChangeListener {
            when (it) {
                true -> {
                    binding.phoneNumberTextInputLayout.error = null
                }

                false -> {
                    if (pageOpenedForFirstTime) {
                        pageOpenedForFirstTime = false
                    } else {
                        if (binding.phoneNumberTextInputEditText.text?.isEmpty() == false) binding.phoneNumberTextInputLayout.error =
                            "Invalid phone number format"
                    }
                }
            }
        }

        binding.continueButton.setOnClickListener {
            val input = binding.phoneNumberTextInputEditText.text.toString()
            if (input.isBlank()) {
                binding.phoneNumberTextInputLayout.error = "Please enter phone number to continue."
                return@setOnClickListener
            }
            if (!binding.countryCodePicker.isValidFullNumber) {
                binding.phoneNumberTextInputLayout.error = "Invalid phone number format"
                return@setOnClickListener
            }
            val prefix = binding.countryCodePicker.selectedCountryCode
            val prefixWithPlus = "+".plus(prefix)
            val fullPhoneNumber = binding.countryCodePicker.fullNumber
            viewModel.getLoginMode(
                prefix = prefixWithPlus,
                phoneNumber = fullPhoneNumber.removeRange(0, prefix.length)
            )
        }

        viewModel.loginMode.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressLinearLayout.visibility = View.VISIBLE
                when (it.status) {
                    DataStatus.Status.Failed -> {
                        binding.continueButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = "${it.message}"
                        viewModel.resetLoginMode()
                    }

                    DataStatus.Status.Loading -> {
                        binding.continueButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.please_wait)
                    }

                    DataStatus.Status.Success -> {
                        binding.continueButton.isEnabled = false
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = it.message
                        binding.phoneNumberTextInputEditText.text = null
                        it.data?.let { loginMode ->
                            when (loginMode.mode) {
                                0 -> {
                                    //send otp
                                    val phoneNumberData = loginMode.phoneNumberData
                                    val phoneNumberPrefix = phoneNumberData.prefix
                                    val phoneNumber = phoneNumberData.phoneNumber

                                    navigateToOTPVerificationPage(
                                        phoneNumber,
                                        phoneNumberPrefix,
                                        false,
                                        false
                                    )
                                    viewModel.resetLoginMode()
                                }

                                1 -> {
                                    //password auth
                                    val phoneNumberData = loginMode.phoneNumberData
                                    val userAccountId = loginMode.userAccountId
                                    val phoneNumber = phoneNumberData.phoneNumber
                                    val prefix = phoneNumberData.prefix
                                    userAccountId?.let {
                                        navigateToPasswordPage(
                                            userAccountId,
                                            phoneNumber,
                                            prefix
                                        )
                                    }
                                    viewModel.resetLoginMode()
                                }

                                else -> {
                                    //there was an error
                                    binding.progressStatusValueTextView.text =
                                        getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                                    viewModel.resetLoginMode()
                                }
                            }
                        }
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.continueButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.no_internet_connection_please_try_again)
                        viewModel.resetLoginMode()
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.continueButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.time_out_please_try_again)
                        viewModel.resetLoginMode()
                    }

                    DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                        requireContext(),
                        it.message
                    )

                    else -> {
                        binding.continueButton.isEnabled = true

                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                        viewModel.resetLoginMode()
                    }
                }
            }
        }
    }

    private fun navigateToPasswordPage(
        userAccountId: String,
        phoneNumber: String,
        prefix: String
    ) {
        val directions =
            LoginOrSignUpFragmentDirections.loginOrSignUpFragmentToPasswordLoginFragment(
                userAccountId = userAccountId,
                phoneNumber = phoneNumber,
                prefix = prefix
            )
        rootView.findNavController().navigate(directions)
    }

    private fun navigateToOTPVerificationPage(
        phoneNumber: String,
        prefix: String,
        forceSendOTP: Boolean,
        passwordAuthFailed: Boolean
    ) {
        val directions =
            LoginOrSignUpFragmentDirections.loginOrSignUpFragmentToOTPVerificationFragment(
                phoneNumber = phoneNumber,
                prefix = prefix,
                forceSendOTP = forceSendOTP,
                passwordAuthFailed = passwordAuthFailed
            )
        rootView.findNavController().navigate(directions)
    }

    private fun openLink(link: String) {
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireContext().startActivity(intent)
    }

}