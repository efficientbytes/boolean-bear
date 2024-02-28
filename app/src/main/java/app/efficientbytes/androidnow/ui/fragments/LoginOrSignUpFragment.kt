package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentLoginOrSignUpBinding
import app.efficientbytes.androidnow.utils.validatePhoneNumberFormat

class LoginOrSignUpFragment : Fragment() {

    private val tagLoginOrSignUpFragment: String = "Login-Sign-Up-Fragment"
    private lateinit var _binding: FragmentLoginOrSignUpBinding
    private val binding get() = _binding
    private lateinit var rootView: View

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
            if (validatePhoneNumberFormat(binding.phoneNumberTextInputLayout,input)) {
                binding.phoneNumberTextInputEditText.text=null
                //make server call to verify by adding prefix
                it.findNavController().navigate(R.id.loginOrSignUpFragment_to_OTPVerificationFragment)
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
    }

}