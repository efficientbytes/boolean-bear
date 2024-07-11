package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentOTPVerificationBinding
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.utils.validateOTPFormat
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import app.efficientbytes.booleanbear.viewmodels.PhoneNumberOTPVerificationViewModel
import `in`.aabhasjindal.otptextview.OTPListener
import org.koin.android.ext.android.inject

class OTPVerificationFragment : Fragment() {

    private lateinit var _binding: FragmentOTPVerificationBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: PhoneNumberOTPVerificationViewModel by viewModels()
    private val mainViewModel: MainViewModel by inject()
    private val safeArgs: OTPVerificationFragmentArgs by navArgs()
    private lateinit var timer: CountDownTimer

    //response data
    private var profileUpdated: Boolean = false
    private var passwordCreated: Boolean = false
    private lateinit var singleDeviceLogin: SingleDeviceLogin
    private lateinit var userAccountId: String
    private lateinit var responsePhoneNumberData: PhoneNumber

    //nav args
    private lateinit var phoneNumber: String
    private lateinit var prefix: String
    private var forceSendOTP: Boolean = false
    private var passwordAuthFailed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOTPVerificationBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneNumber = safeArgs.phoneNumber
        prefix = safeArgs.prefix
        forceSendOTP = safeArgs.forceSendOTP
        passwordAuthFailed = safeArgs.passwordAuthFailed

        if (forceSendOTP) {
            viewModel.sendOTPToPhoneNumber(prefix = prefix, phoneNumber = phoneNumber)
        }
        //timer
        binding.resendOtpChip.visibility = View.VISIBLE
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.resendOtpChip.isEnabled = false
                binding.resendOtpChip.text = getString(
                    R.string.resend_otp_by_sms_in_secs,
                    (millisUntilFinished / 1000).toString()
                )
            }

            override fun onFinish() {
                binding.resendOtpChip.isEnabled = true
                binding.resendOtpChip.text = getString(R.string.resend_otp)
            }
        }
        if (!forceSendOTP) {
            timer.start()
        }

        if (forceSendOTP) {
            binding.otpSentToLabelTextView.visibility = View.INVISIBLE
        } else {
            binding.otpSentToLabelTextView.visibility = View.VISIBLE
            binding.otpSentToLabelTextView.text =
                getString(R.string.otp_sent_to, prefix, phoneNumber)
            binding.otpPinViewLayout.requestFocusOTP()
        }

        binding.otpPinViewLayout.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                if (validateOTPFormat(otp)) {
                    viewModel.verifyPhoneNumberOTP(prefix, phoneNumber, otp)
                }
            }
        }

        binding.takeMeToHomePageButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        binding.resendOtpChip.setOnClickListener {
            viewModel.sendOTPToPhoneNumber(prefix = prefix, phoneNumber = phoneNumber)
        }

        viewModel.verifyPhoneNumberOTPResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.otpPinViewLayout.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.otpPinViewLayout.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_verify_the_entered_otp)
                }

                DataStatus.Status.Success -> {
                    binding.otpPinViewLayout.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    //request for sign in token since user phone number is verified successfully
                    it.data?.let { phoneNumberData ->
                        mainViewModel.getSignInToken(
                            phoneNumberData.prefix,
                            phoneNumberData.phoneNumber
                        )
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

        mainViewModel.signInToken.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_sign_you_in)
                }

                DataStatus.Status.Success -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    //sign the user with the received sign in token
                    it.data?.let { signInToken ->
                        singleDeviceLogin = signInToken.singleDeviceLogin
                        userAccountId = signInToken.userAccountId
                        profileUpdated = signInToken.basicProfileDetailsUpdated
                        passwordCreated = signInToken.passwordCreated
                        responsePhoneNumberData = signInToken.phoneNumberData
                        mainViewModel.signInWithToken(signInToken)
                        mainViewModel.updatePasswordCreatedFlag(passwordCreated)
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

        mainViewModel.isUserSignedIn.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_sign_you_in)
                }

                DataStatus.Status.Success -> {
                    when (it.data) {
                        true -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.text =
                                getString(R.string.you_have_been_signed_in_successfully)
                            mainViewModel.saveSingleDeviceLogin(singleDeviceLogin)
                            mainViewModel.getUserProfileFromRemote()
                            mainViewModel.getAllWaitingListCourses()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.signed_in_successfully),
                                Toast.LENGTH_SHORT
                            ).show()

                            when (passwordAuthFailed) {
                                true -> {
                                    //user needs to go to manage password
                                    val directions =
                                        OTPVerificationFragmentDirections.otpVerificationFragmentToManagePasswordFragment(
                                            3
                                        )
                                    findNavController().navigate(directions)
                                }

                                false -> {
                                    // proceed with normal flow
                                    when (profileUpdated) {
                                        true -> {
                                            //go to either password page or home page
                                            if (passwordCreated) {
                                                findNavController().popBackStack(
                                                    R.id.homeFragment,
                                                    false
                                                )
                                            } else {
                                                val directions =
                                                    OTPVerificationFragmentDirections.otpVerificationFragmentToManagePasswordFragment(
                                                        1
                                                    )
                                                findNavController().navigate(directions)
                                            }
                                        }

                                        false -> {
                                            //go to complete profile page
                                            val directions =
                                                OTPVerificationFragmentDirections.otpVerificationFragmentToCompleteProfileFragment(
                                                    responsePhoneNumberData.prefix,
                                                    responsePhoneNumberData.phoneNumber,
                                                    userAccountId,
                                                    passwordCreated,
                                                )
                                            findNavController().navigate(directions)
                                        }
                                    }
                                }
                            }
                        }

                        else -> {

                        }
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

        viewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
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
                        getString(R.string.please_wait_while_we_resend_the_otp)
                }

                DataStatus.Status.Success -> {
                    binding.otpSentToLabelTextView.visibility = View.VISIBLE
                    binding.otpSentToLabelTextView.text =
                        getString(R.string.otp_sent_to, it.data?.prefix, it.data?.phoneNumber)
                    binding.otpPinViewLayout.requestFocusOTP()
                    binding.progressLinearLayout.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    timer.start()
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
    }

    private fun noInternetResponse() {
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.no_internet_connection_please_try_again)
        binding.takeMeToHomePageButton.visibility = View.VISIBLE
    }

    private fun timeOutResponse() {
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.time_out_please_try_again)
        binding.takeMeToHomePageButton.visibility = View.VISIBLE
    }

    private fun unknownExceptionResponse() {
        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.VISIBLE
        binding.progressStatusValueTextView.text =
            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
        binding.takeMeToHomePageButton.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        timer.cancel()
    }

}