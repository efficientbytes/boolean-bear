package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentLoginToContinueBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoginToContinueFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentLoginToContinueBinding
    private val binding get() = _binding
    private lateinit var rootView: View

    companion object {

        const val LOGIN_TO_CONTINUE_FRAGMENT: String = "frag-login-to-continue"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginToContinueBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.homeFragment_to_loginOrSignUpFragment)
        }
    }

}