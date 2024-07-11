package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentVerifyReporterBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.SingletonRequestSupport
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import `in`.aabhasjindal.otptextview.OTPListener
import org.koin.android.ext.android.inject

class VerifyReporterFragment : Fragment() {

    private lateinit var _binding: FragmentVerifyReporterBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var phoneNumber: String = ""
    private var prefix: String = ""
    private val mainViewModel: MainViewModel by inject()
    private val safeArgs: VerifyReporterFragmentArgs by navArgs()
    private lateinit var timer: CountDownTimer

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

        this.prefix = safeArgs.prefix
        this.phoneNumber = safeArgs.phoneNumber

        binding.resendOtpChip.visibility = View.VISIBLE
        binding.resendOtpChip.isEnabled = false

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.resendOtpChip.isEnabled = false
                binding.resendOtpChip.text =
                    getString(
                        R.string.resend_otp_by_sms_in_secs,
                        (millisUntilFinished / 1000).toString()
                    )
            }

            override fun onFinish() {
                binding.resendOtpChip.isEnabled = true
                binding.resendOtpChip.text = getString(R.string.resend_otp)
            }
        }

        timer.start()
        binding.otpHasBeenSentLabelTextView.text =
            getString(R.string.otp_sent_to, prefix, phoneNumber)

        binding.otpPinViewLayout.requestFocusOTP()
        binding.otpPinViewLayout.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                mainViewModel.verifyPhoneNumberOTP(
                    prefix = prefix,
                    phoneNumber = phoneNumber,
                    otp = otp
                )
            }
        }

        mainViewModel.verifyOtpStatus.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.otpPinViewLayout.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                }

                DataStatus.Status.Loading -> {
                    binding.otpPinViewLayout.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_verify_the_entered_otp)
                }

                DataStatus.Status.Success -> {
                    binding.progressStatusValueTextView.text = it.message
                    val singletonRequestSupport = SingletonRequestSupport.getInstance()
                    it.data?.let { phoneNumberData ->
                        singletonRequestSupport?.apply {
                            userAccountId = null
                            prefix = phoneNumberData.prefix
                            phoneNumber = phoneNumberData.phoneNumber
                            completePhoneNumber = prefix + phoneNumber
                        }
                    }
                    singletonRequestSupport?.let { requestSupport ->
                        mainViewModel.requestSupport(requestSupport)
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.otpPinViewLayout.isEnabled = true
                    noInternetResponse()
                }

                DataStatus.Status.TimeOut -> {
                    binding.otpPinViewLayout.isEnabled = true
                    timeOutResponse()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.otpPinViewLayout.isEnabled = true
                    unknownExceptionResponse()
                }
            }
        }

        mainViewModel.requestSupportResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait)
                }

                DataStatus.Status.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    it.data?.let { requestSupportStatus ->
                        val direction =
                            VerifyReporterFragmentDirections.verifyReporterFragmentToReportSubmittedFragment(
                                requestSupportStatus.message,
                                requestSupportStatus.ticketId
                            )
                        findNavController().navigate(direction)
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                DataStatus.Status.NoInternet -> {
                    noInternetResponse()
                }

                DataStatus.Status.TimeOut -> {
                    timeOutResponse()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    unknownExceptionResponse()
                }
            }
        }
        binding.resendOtpChip.setOnClickListener {
            mainViewModel.sendOTPToPhoneNumber(prefix = prefix, phoneNumber = phoneNumber)
        }
        mainViewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.resendOtpChip.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                }

                DataStatus.Status.Loading -> {
                    binding.resendOtpChip.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_resend_the_otp)
                }

                DataStatus.Status.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    timer.start()
                }

                DataStatus.Status.NoInternet -> {
                    binding.resendOtpChip.isEnabled = true
                    noInternetResponse()
                }

                DataStatus.Status.TimeOut -> {
                    binding.resendOtpChip.isEnabled = true
                    timeOutResponse()
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.resendOtpChip.isEnabled = true
                    unknownExceptionResponse()
                }
            }
        }

    }

    private fun noInternetResponse() {
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.no_internet_connection_please_try_again)
    }

    private fun timeOutResponse() {
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text = getString(R.string.time_out_please_try_again)
    }

    private fun unknownExceptionResponse() {
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
    }

    override fun onDetach() {
        super.onDetach()
        timer.cancel()
    }

}