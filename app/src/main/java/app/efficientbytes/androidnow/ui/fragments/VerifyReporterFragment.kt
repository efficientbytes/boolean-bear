package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.databinding.FragmentVerifyReporterBinding
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.SingletonRequestSupport
import app.efficientbytes.androidnow.services.models.VerifyPhoneNumber
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import `in`.aabhasjindal.otptextview.OTPListener
import org.koin.android.ext.android.inject

class VerifyReporterFragment : Fragment() {

    private lateinit var _binding: FragmentVerifyReporterBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var phoneNumber: String = ""
    private val mainViewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = VerifyReporterFragmentArgs.fromBundle(bundle)
        val phoneNumber = args.phoneNumber
        this.phoneNumber = phoneNumber
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyReporterBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.otpHasBeenSentLabelTextView.text = "OTP has been sent to $phoneNumber"

        binding.otpPinViewLayout.requestFocusOTP()
        binding.otpPinViewLayout.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                mainViewModel.verifyPhoneNumberOTP(VerifyPhoneNumber(phoneNumber, otp))
            }
        }

        mainViewModel.verifyOtpStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.otpPinViewLayout.isEnabled = true
                    binding.otpPinViewLayout.otpListener = object : OTPListener {
                        override fun onInteractionListener() {

                        }

                        override fun onOTPComplete(otp: String) {
                            mainViewModel.verifyPhoneNumberOTP(VerifyPhoneNumber(phoneNumber, otp))
                        }
                    }
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                }

                DataStatus.Status.Loading -> {
                    binding.otpPinViewLayout.isEnabled = false
                    binding.otpPinViewLayout.otpListener = null
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we verify the entered OTP..."
                }

                DataStatus.Status.Success -> {
                    binding.progressStatusValueTextView.text = "OTP Verified successfully"
                    val singletonRequestSupport = SingletonRequestSupport.getInstance()
                    singletonRequestSupport?.apply {
                        userAccountId = null
                        completePhoneNumber = "+91".plus(it.data?.phoneNumber)
                    }
                    singletonRequestSupport?.let { requestSupport ->
                        mainViewModel.requestSupport(requestSupport)
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
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we submit your issue."
                }

                DataStatus.Status.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Successfully submitted."
                    val responseBody = it.data
                    responseBody?.let { requestSupportStatus ->
                        val direction =
                            VerifyReporterFragmentDirections.verifyReporterFragmentToReportSubmittedFragment(
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