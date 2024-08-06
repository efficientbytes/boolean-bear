package app.efficientbytes.booleanbear.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentAccountSettingsBinding
import app.efficientbytes.booleanbear.utils.AppAuthStateListener
import app.efficientbytes.booleanbear.viewmodels.AccountSettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject

class AccountSettingsFragment : BottomSheetDialogFragment() {

    companion object {

        var isOpened: Boolean = false
    }

    private lateinit var _binding: FragmentAccountSettingsBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val appAuthStateListener: AppAuthStateListener by inject()
    private val viewModel: AccountSettingsViewModel by viewModels()
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
        binding.customAuthState = appAuthStateListener
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginOrSignUpCardView.setOnClickListener {
            findNavController().navigate(R.id.accountSettingsFragment_to_loginOrSignUpFragment)
            dismiss()
        }
        binding.accountSettingsCardView.setOnClickListener {
            findNavController().navigate(R.id.accountSettingsFragment_to_editProfileFragment)
            dismiss()
        }
        viewModel.userProfile.observe(viewLifecycleOwner) {
            if (auth.currentUser != null) {
                it?.let { userProfile ->
                    binding.firstNameValueTextView.text =
                        getString(R.string.hi, userProfile.firstName)
                }
            }
        }
        binding.shareAppLinkCardView.setOnClickListener {
            dismiss()
            shareAppLink()
        }
        binding.getInTouchWithUsCardView.setOnClickListener {
            findNavController().navigate(R.id.accountSettingsFragment_to_contactUsFragment)
            dismiss()
        }
    }

    private fun shareAppLink() {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, "boolean bear")
        var shareMessage =
            "\n\n The only dedicated platform for learning C++ and Kotlin\n\n"
        shareMessage =
            shareMessage + "https://play.google.com/store/apps/details?id=" + requireContext().packageName + "\n"
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(intent, "Select One"))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isOpened = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpened = false
    }

    override fun onDetach() {
        super.onDetach()
        isOpened = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOpened = false
    }
}