package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentLoginOrSignUpBinding

class LoginOrSignUpFragment : Fragment() {

    private val tagCoursesFragment: String = "Login-Sign-Up-Fragment"
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
            it.findNavController().navigate(R.id.loginOrSignUpFragment_to_OTPVerificationFragment)
        }
    }

}