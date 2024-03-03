package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentOTPVerificationBinding
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.services.models.PhoneNumber
import app.efficientbytes.androidnow.utils.validateOTPFormat
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import app.efficientbytes.androidnow.viewmodels.PhoneNumberOTPVerificationViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
        binding.otpSentToLabelTextView.text = "OTP sent to +91${phoneNumber}"
        binding.verifyButton.setOnClickListener {
            binding.otpPinViewLayout.otp?.apply {
                if (validateOTPFormat(this)) {
                    viewModel.verifyPhoneNumberOTP(phoneNumber, this)
                }
            }
        }
        binding.otpPinViewLayout.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                binding.otpPinViewLayout.otp?.apply {
                    if (this.length < 6) binding.verifyButton.isEnabled = false
                }
            }

            override fun onOTPComplete(otp: String) {
                binding.verifyButton.isEnabled = true
            }
        }
        viewModel.verifyPhoneNumberOTPResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.verifyButton.isEnabled = true
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we verify the entered OTP..."
                }

                DataStatus.Status.Success -> {
                    binding.verifyButton.isEnabled = false
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
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we sign you in..."
                }

                DataStatus.Status.Success -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    //sign the user with the received sign in token
                    it.data?.let { signInToken ->
                        mainViewModel.signInWithToken(signInToken)
                    }
                }
            }
        }
        mainViewModel.isUserSignedIn.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    binding.takeMeToHomePageButton.visibility = View.VISIBLE
                }

                DataStatus.Status.Loading -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we sign you in..."
                }

                DataStatus.Status.Success -> {
                    when (it.data) {
                        true -> {
                            binding.verifyButton.isEnabled = false
                            binding.progressBar.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.text =
                                "You have been signed in successfully"
                            //get the firebase user token so that we can extract the custom claims
                            mainViewModel.getFirebaseUserToken()
                        }

                        false -> {

                        }

                        null -> {

                        }
                    }
                }
            }
        }
        mainViewModel.firebaseUserToken.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "${it.message}"
                    findNavController().popBackStack(R.id.coursesFragment, false)
                }

                DataStatus.Status.Loading -> {
                    binding.verifyButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Getting user profile..."
                }

                DataStatus.Status.Success -> {
                    it.data?.let { token ->
                        val profileUpdated: Any? = token.claims["profileUpdated"]
                        profileUpdated?.let { claim ->
                            if (claim is Boolean) {
                                if (claim == false) { // if profile is not updated , show setup profile
                                    val userId = Firebase.auth.currentUser?.uid
                                    userId?.let { userAccountId ->
                                        val directions =
                                            OTPVerificationFragmentDirections.otpVerificationFragmentToCompleteProfileFragment(
                                                phoneNumber,
                                                userAccountId
                                            )
                                        findNavController().navigate(directions)
                                    }
                                } else { // if profile is updated , go to course fragment
                                    findNavController().popBackStack(R.id.coursesFragment, false)
                                }
                            }
                        }
                    }
                }
            }
        }
        binding.takeMeToHomePageButton.setOnClickListener {
            findNavController().popBackStack(R.id.coursesFragment, false)
        }
    }

}