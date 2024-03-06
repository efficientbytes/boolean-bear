package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentAccountSettingsBinding
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject

class AccountSettingsFragment : Fragment() {

    private val tagCoursesFragment: String = "Account-Settings-Fragment"
    private lateinit var _binding: FragmentAccountSettingsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by inject()
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainViewModel = mainViewModel
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginOrSignUpLabelTextView.setOnClickListener {
            it.findNavController().navigate(R.id.accountSettingsFragment_to_loginOrSignUpFragment)
        }
        mainViewModel.authState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    true -> {
                        userSignIn()
                    }

                    false -> {
                        userSignedOut()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userSignIn()
        } else {
            userSignedOut()
        }
    }

    private fun userSignedOut() {
        binding.loginOrSignUpLabelTextView.visibility = View.VISIBLE
        binding.hiUserValueTextView.visibility = View.GONE
        binding.goodToHaveYouLabelTextView.visibility = View.GONE
    }

    private fun userSignIn() {
        binding.loginOrSignUpLabelTextView.visibility = View.GONE
        binding.hiUserValueTextView.visibility = View.VISIBLE
        binding.goodToHaveYouLabelTextView.visibility = View.VISIBLE
    }

}