package app.efficientbytes.androidnow.ui.fragments

import android.app.Dialog
import android.content.Intent
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
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        binding.editProfileLabelTextView.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.action_coursesFragment_to_editProfileFragment)
        }
        viewModel.userProfile.observe(viewLifecycleOwner) {
            if (auth.currentUser != null) {
                it?.let { userProfile ->
                    binding.hiUserValueTextView.text = "Hi ${userProfile.firstName}"
                }
            }
        }
        binding.inviteFriendsLabelTextView.setOnClickListener {
            inviteFriends()
        }
    }

    private fun inviteFriends() {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Android Now")
        var shareMessage =
            "\n\n The only dedicated complete android development learning platform\n\n"
        shareMessage =
            shareMessage + "https://play.google.com/store/apps/details?id=" + requireContext().packageName + "\n"
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(intent, "Select One"))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as? BottomSheetDialog)
                ?.behavior
                ?.setPeekHeight(800, true)
        }
    }
}