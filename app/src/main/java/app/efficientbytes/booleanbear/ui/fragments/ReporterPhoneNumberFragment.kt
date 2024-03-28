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
import app.efficientbytes.booleanbear.databinding.FragmentReporterPhoneNumberBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.SingletonRequestSupport
import app.efficientbytes.booleanbear.utils.validatePhoneNumberFormat
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class ReporterPhoneNumberFragment : Fragment() {

    private lateinit var _binding: FragmentReporterPhoneNumberBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by inject()

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
            binding.phoneNumberVerifiedMessageLabelTextView.visibility = View.VISIBLE
            val phoneNumber = SingletonUserData.getInstance()?.completePhoneNumber
            binding.phoneNumberInputEditText.setText(SingletonUserData.getInstance()?.phoneNumber)
            binding.phoneNumberTextInputLayout.isEnabled = false
            binding.phoneNumberTextInputLayout.isClickable = false
            binding.phoneNumberTextInputLayout.isLongClickable = false
            binding.phoneNumberInputEditText.isClickable = false
            binding.phoneNumberInputEditText.isLongClickable = false
            binding.submitButton.visibility = View.VISIBLE
            binding.continueButton.visibility = View.GONE
            val requestSupport = SingletonRequestSupport.getInstance()
            requestSupport?.apply {
                userAccountId = currentUser.uid
                completePhoneNumber = phoneNumber
            }
        } else {
            binding.phoneNumberVerifiedMessageLabelTextView.visibility = View.GONE
            binding.continueButton.visibility = View.VISIBLE
            binding.submitButton.visibility = View.GONE
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
            if (validatePhoneNumberFormat(binding.phoneNumberTextInputLayout, input)) {
                mainViewModel.sendOTPToPhoneNumber(input)
            }
        }

        binding.phoneNumberInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.phoneNumberTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        mainViewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
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
                    it.data?.phoneNumber?.also { phoneNumber ->
                        val directions =
                            ReporterPhoneNumberFragmentDirections.reporterPhoneNumberFragmentToVerifyReporterFragment(
                                phoneNumber
                            )
                        rootView.findNavController().navigate(directions)
                    }
                }
            }
        }

        mainViewModel.requestSupportResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                }

                DataStatus.Status.Loading -> {
                    binding.submitButton.isEnabled = false
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we submit your issue."
                }

                DataStatus.Status.Success -> {
                    binding.submitButton.isEnabled = false
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Successfully submitted."
                    val responseBody = it.data
                    responseBody?.let { requestSupportStatus ->
                        val direction =
                            ReporterPhoneNumberFragmentDirections.reporterPhoneNumberFragmentToReportSubmittedFragment(
                                requestSupportStatus.message,
                                requestSupportStatus.ticketId
                            )
                        findNavController().navigate(direction)
                    }
                }
            }
        }

    }

}