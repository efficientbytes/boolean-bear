package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.booleanbear.databinding.FragmentLoginOrSignUpBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.validatePhoneNumberFormat
import app.efficientbytes.booleanbear.viewmodels.LoginOrSignUpViewModel
import org.koin.android.ext.android.inject

class LoginOrSignUpFragment : Fragment() {

    private lateinit var _binding: FragmentLoginOrSignUpBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: LoginOrSignUpViewModel by inject()
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
        binding.continueButton.setOnClickListener {
            val input = binding.phoneNumberTextInputEditText.text.toString()
            if (validatePhoneNumberFormat(binding.phoneNumberTextInputLayout, input)) {
                viewModel.sendOTPToPhoneNumber(input)
            }
        }
        binding.phoneNumberTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.phoneNumberTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        viewModel.sendOTPToPhoneNumberResponse.observe(viewLifecycleOwner) {
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
                    binding.continueButton.isEnabled = false
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.data?.message
                    binding.phoneNumberTextInputEditText.text = null
                    it.data?.phoneNumber?.also { phoneNumber ->
                        val directions =
                            LoginOrSignUpFragmentDirections.loginOrSignUpFragmentToOTPVerificationFragment(
                                phoneNumber
                            )
                        rootView.findNavController().navigate(directions)
                    }
                }
            }
        }
    }

}