package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.databinding.FragmentVerifyReporterBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.SingletonRequestSupport
import app.efficientbytes.booleanbear.services.models.VerifyPhoneNumber
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import `in`.aabhasjindal.otptextview.OTPListener
import org.koin.android.ext.android.inject

class VerifyReporterFragment : Fragment() {

    private lateinit var _binding: FragmentVerifyReporterBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var phoneNumber: String = ""
    private val mainViewModel: MainViewModel by inject()
    private lateinit var timer: CountDownTimer

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
        binding.resendOtpChip.visibility = View.VISIBLE
        binding.resendOtpChip.isEnabled = false
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.resendOtpChip.text =
                    "Resend OTP by SMS in ${millisUntilFinished / 1000} secs"
            }

            override fun onFinish() {
                binding.resendOtpChip.isEnabled = true
                binding.resendOtpChip.text = "Resend OTP"
            }
        }
        timer.start()
        binding.otpHasBeenSentLabelTextView.text = "OTP has been sent to +91$phoneNumber"

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
        binding.resendOtpChip.setOnClickListener {
            mainViewModel.sendOTPToPhoneNumber(phoneNumber)
        }
        mainViewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.resendOtpChip.isEnabled = false
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                }

                DataStatus.Status.Loading -> {
                    binding.resendOtpChip.isEnabled = false
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we resend the OTP..."
                }

                DataStatus.Status.Success -> {
                    binding.progressLinearLayout.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.GONE
                    Toast.makeText(requireContext(), it.data?.message, Toast.LENGTH_LONG).show()
                    binding.resendOtpChip.visibility = View.GONE
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
    }

}