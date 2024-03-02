package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.databinding.FragmentOTPVerificationBinding
import app.efficientbytes.androidnow.utils.validateOTPFormat
import `in`.aabhasjindal.otptextview.OTPListener

class OTPVerificationFragment : Fragment() {

    private val tagOTPVerification: String = "OTP-Verification-Fragment"
    private lateinit var _binding: FragmentOTPVerificationBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private lateinit var phoneNumber: String

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
        binding.verifyButton.setOnClickListener {
            binding.otpPinViewLayout.otp?.apply {
                if (validateOTPFormat(this)) {
                    //make server call to verify the otp
                    val directions = OTPVerificationFragmentDirections.otpVerificationFragmentToCompleteProfileFragment(phoneNumber)
                    rootView.findNavController().navigate(directions)
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
    }

}