package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentReporterPhoneNumberBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.SingletonRequestSupport
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class ReporterPhoneNumberFragment : Fragment() {

    private lateinit var _binding: FragmentReporterPhoneNumberBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by inject()
    private var pageOpenedForFirstTime = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReporterPhoneNumberBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            binding.countryCodePicker.visibility = View.GONE
            binding.phoneNumberVerifiedMessageLabelTextView.visibility = View.VISIBLE

            binding.phoneNumberTextInputLayout.prefixText =
                SingletonUserData.getInstance()?.phoneNumberPrefix
            binding.phoneNumberInputEditText.setText(SingletonUserData.getInstance()?.phoneNumber)

            binding.phoneNumberTextInputLayout.isEnabled = false
            binding.phoneNumberTextInputLayout.isClickable = false
            binding.phoneNumberTextInputLayout.isLongClickable = false
            binding.phoneNumberInputEditText.isClickable = false
            binding.phoneNumberInputEditText.isLongClickable = false
            binding.submitButton.visibility = View.VISIBLE
            binding.continueButton.visibility = View.GONE
            val requestSupport = SingletonRequestSupport.getInstance()
            SingletonUserData.getInstance()?.let { userProfile ->
                requestSupport?.apply {
                    userAccountId = userProfile.userAccountId
                    prefix = userProfile.phoneNumberPrefix
                    phoneNumber = userProfile.phoneNumber
                    completePhoneNumber = prefix + phoneNumber
                }
            }
        } else {
            binding.countryCodePicker.visibility = View.VISIBLE
            binding.phoneNumberVerifiedMessageLabelTextView.visibility = View.GONE
            binding.continueButton.visibility = View.VISIBLE
            binding.submitButton.visibility = View.GONE

            binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumberInputEditText)
            binding.countryCodePicker.setPhoneNumberValidityChangeListener {
                when (it) {
                    true -> {
                        binding.phoneNumberTextInputLayout.error = null
                    }

                    false -> {
                        if (pageOpenedForFirstTime) {
                            pageOpenedForFirstTime = false
                        } else {
                            if (binding.phoneNumberInputEditText.text?.isEmpty() == false) binding.phoneNumberTextInputLayout.error =
                                "Invalid phone number format"
                        }
                    }
                }
            }

        }

        binding.submitButton.setOnClickListener {
            //upload the report
            val singletonRequestSupport = SingletonRequestSupport.getInstance()
            singletonRequestSupport?.let { requestSupport ->
                mainViewModel.requestSupport(requestSupport)
            }
        }

        binding.continueButton.setOnClickListener {
            val input = binding.phoneNumberInputEditText.text.toString()
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
            mainViewModel.sendOTPToPhoneNumber(
                prefix = prefixWithPlus,
                phoneNumber = fullPhoneNumber.removeRange(0, prefix.length)
            )
        }

        binding.phoneNumberInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.phoneNumberInputEditText.text?.isEmpty() == true) {
                    binding.phoneNumberTextInputLayout.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        mainViewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.continueButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                }

                DataStatus.Status.Loading -> {
                    binding.continueButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = getString(R.string.please_wait)
                }

                DataStatus.Status.Success -> {
                    it.data?.let { result ->
                        binding.continueButton.isEnabled = false
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.text = it.message
                        result
                    }?.let { phoneNumberData ->
                        val directions =
                            ReporterPhoneNumberFragmentDirections.reporterPhoneNumberFragmentToVerifyReporterFragment(
                                phoneNumberData.prefix, phoneNumberData.phoneNumber
                            )
                        rootView.findNavController().navigate(directions)
                    }
                }

                DataStatus.Status.NoInternet -> noInternetResponse()
                DataStatus.Status.TimeOut -> timeOutResponse()
                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )
                else -> unknownExceptionResponse()
            }
        }

        mainViewModel.requestSupportResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                }

                DataStatus.Status.Loading -> {
                    binding.submitButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait)
                }

                DataStatus.Status.Success -> {
                    binding.submitButton.isEnabled = false
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    it.data?.let { result ->
                        val direction =
                            ReporterPhoneNumberFragmentDirections.reporterPhoneNumberFragmentToReportSubmittedFragment(
                                result.message,
                                result.ticketId
                            )
                        findNavController().navigate(direction)
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.submitButton.isEnabled = true
                    noInternetResponse()
                }

                DataStatus.Status.TimeOut -> {
                    binding.submitButton.isEnabled = true
                    timeOutResponse()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.submitButton.isEnabled = true
                    unknownExceptionResponse()
                }
            }
        }

    }

    private fun noInternetResponse() {
        binding.continueButton.isEnabled = true
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.no_internet_connection_please_try_again)
    }

    private fun timeOutResponse() {
        binding.continueButton.isEnabled = true
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.time_out_please_try_again)
    }

    private fun unknownExceptionResponse() {
        binding.continueButton.isEnabled = true
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
    }

}