package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentAccountSettingsBinding
import app.efficientbytes.androidnow.viewmodels.AccountSettingsViewModel
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject

class AccountSettingsFragment : BottomSheetDialogFragment() {

    companion object {
        val tagAccountSettings: String = "Account-Settings-Fragment"
    }
    private lateinit var _binding: FragmentAccountSettingsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
    private val viewModel: AccountSettingsViewModel by inject()
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
            dismiss()
            findNavController().navigate(R.id.coursesFragment_to_loginOrSignUpFragment)
        }
        viewModel.userProfile.observe(viewLifecycleOwner) {
            if (auth.currentUser != null) {
                it?.let { userProfile ->
                    binding.hiUserValueTextView.text = "Hi ${userProfile.firstName}"
                }
            }
        }
    }

}