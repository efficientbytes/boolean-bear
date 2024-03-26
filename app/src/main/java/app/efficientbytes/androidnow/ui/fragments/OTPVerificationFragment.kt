package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentOTPVerificationBinding
import app.efficientbytes.androidnow.models.SingleDeviceLogin
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.utils.validateOTPFormat
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import app.efficientbytes.androidnow.viewmodels.PhoneNumberOTPVerificationViewModel
import `in`.aabhasjindal.otptextview.OTPListener
import org.koin.android.ext.android.inject

class OTPVerificationFragment : Fragment() {

    private val tagOTPVerification: String = "OTP-Verification-Fragment"
    private lateinit var _binding: FragmentOTPVerificationBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private lateinit var phoneNumber: String
    private val viewModel: PhoneNumberOTPVerificationViewModel by inject()
    private val mainViewModel: MainViewModel by inject()
    private var profileUpdated: Boolean? = false
    private var userAccountId: String? = null
    private var singleDeviceLogin: SingleDeviceLogin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = OTPVerificationFragmentArgs.fromBundle(bundle)
        val phoneNumber = args.phoneNumber
        this.phoneNumber = phoneNumber
    }

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
        binding.resendOtpChip.isEnabled = false
        binding.resendOtpChip.visibility = View.VISIBLE
        binding.otpSentToLabelTextView.text = "OTP sent to +91${phoneNumber}"
        binding.otpPinViewLayout.requestFocusOTP()
        binding.otpPinViewLayout.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                if (validateOTPFormat(otp)) {
                    viewModel.verifyPhoneNumberOTP(phoneNumber, otp)
                }
            }
        }

        viewModel.verifyPhoneNumberOTPResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.otpPinViewLayout.isEnabled = true
                    binding.otpPinViewLayout.otpListener = object : OTPListener {
                        override fun onInteractionListener() {

                        }

                        override fun onOTPComplete(otp: String) {
                            if (validateOTPFormat(otp)) {
                                viewModel.verifyPhoneNumberOTP(phoneNumber, otp)
                            }
                        }
                    }
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
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
                    binding.otpPinViewLayout.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.data?.message
                    //request for sign in token since user phone number is verified successfully
                    it.data?.phoneNumber?.also { phoneNumber ->
                        mainViewModel.getSignInToken(PhoneNumber(phoneNumber, "+91"))
                    }
                }
            }
        }
        mainViewModel.signInToken.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we sign you in..."
                }

                DataStatus.Status.Success -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    //sign the user with the received sign in token
                    it.data?.let { signInToken ->
                        singleDeviceLogin = signInToken.singleDeviceLogin
                        userAccountId = signInToken.userAccountId
                        profileUpdated = signInToken.basicProfileDetailsUpdated
                        mainViewModel.signInWithToken(signInToken)
                    }
                }
            }
        }
        mainViewModel.isUserSignedIn.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we sign you in..."
                }

                DataStatus.Status.Success -> {
                    when (it.data) {
                        true -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.text =
                                "You have been signed in successfully"
                            singleDeviceLogin?.let { singleDeviceLogin ->
                                mainViewModel.saveSingleDeviceLogin(singleDeviceLogin)
                            }
                            Toast.makeText(
                                requireContext(),
                                "Signed in successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            userAccountId?.let { userAccountId ->
                                profileUpdated?.let { profileUpdated ->
                                    if (profileUpdated) {
                                        findNavController().popBackStack(
                                            R.id.coursesFragment,
                                            false
                                        )
                                    } else {
                                        val directions =
                                            OTPVerificationFragmentDirections.otpVerificationFragmentToCompleteProfileFragment(
                                                phoneNumber,
                                                userAccountId
                                            )
                                        findNavController().navigate(directions)
                                    }
                                }
                            }
                        }

                        false -> {

                        }

                        null -> {

                        }
                    }
                }
            }
        }
        binding.takeMeToHomePageButton.setOnClickListener {
            findNavController().popBackStack(R.id.coursesFragment, false)
        }
        binding.resendOtpChip.setOnClickListener {
        }
    }

}