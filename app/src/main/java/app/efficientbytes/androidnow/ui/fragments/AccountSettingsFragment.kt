package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentAccountSettingsBinding

class AccountSettingsFragment : Fragment() {

    private val tagCoursesFragment: String = "Account-Settings-Fragment"
    private lateinit var _binding: FragmentAccountSettingsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginOrSignUpLabelTextView.setOnClickListener {
            it.findNavController().navigate(R.id.accountSettingsFragment_to_loginOrSignUpFragment)
        }
    }

}