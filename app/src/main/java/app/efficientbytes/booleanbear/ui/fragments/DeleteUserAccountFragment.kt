package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.DialogDeleteAccountConfirmationBinding
import app.efficientbytes.booleanbear.databinding.FragmentDeleteUserAccountBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeleteUserAccountFragment : Fragment() {

    private lateinit var _binding: FragmentDeleteUserAccountBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteUserAccountBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack(R.id.contactUsFragment, false)
        }
        var opened = false
        binding.deleteNowButton.setOnClickListener {
            val materialAlertDialogBuilder: MaterialAlertDialogBuilder =
                MaterialAlertDialogBuilder(requireContext())
            val layoutInflater = LayoutInflater.from(requireContext())
            val dialogBinding: DialogDeleteAccountConfirmationBinding =
                DialogDeleteAccountConfirmationBinding.inflate(layoutInflater, null, false)
            dialogBinding.deleteInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    dialogBinding.deleteTextInputLayout.error = null
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
            materialAlertDialogBuilder.setView(dialogBinding.root)
                .setOnDismissListener {
                    opened = false
                }
                .setCancelable(true)
            if (!opened) {
                opened = true
                val dialog: AlertDialog = materialAlertDialogBuilder.create()
                dialog.show()
                dialogBinding.proceedButton.setOnClickListener {
                    val input = dialogBinding.deleteInputEditText.text.toString().trim()
                    if (input.isBlank()) {
                        dialogBinding.deleteTextInputLayout.error =
                            "Please enter \"Delete\" to proceed."
                    } else if (input != "DELETE") {
                        dialogBinding.deleteTextInputLayout.error = "Incorrect input."
                    } else {
                        opened = false
                        binding.deleteNowButton.isEnabled = false
                        binding.cancelButton.isEnabled = false
                        dialog.dismiss()
                        val singletonUser = SingletonUserData.getInstance()
                        singletonUser?.let {
                            mainViewModel.deleteUserAccount()
                        }
                    }
                }
            }
        }
        mainViewModel.deleteUserAccountStatus.observe(viewLifecycleOwner) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.Failed -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = it.message
                        binding.takeMeToHomePageButton.visibility = View.VISIBLE
                    }

                    DataStatus.Status.Loading -> {
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.please_wait)
                    }

                    DataStatus.Status.Success -> {
                        binding.progressLinearLayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = it.data?.message
                        lifecycleScope.launch {
                            delay(1200)
                            findNavController().popBackStack(R.id.homeFragment, false)
                        }
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = getString(R.string.no_internet_connection_please_try_again)
                        binding.takeMeToHomePageButton.visibility = View.VISIBLE
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.deleting_account_is_taking_unusually_long_time_please_try_again_after_some_time)
                        binding.takeMeToHomePageButton.visibility = View.VISIBLE
                    }

                    DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                        requireContext(),
                        it.message
                    )

                    else -> {

                    }
                }
            }
            binding.takeMeToHomePageButton.setOnClickListener {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser == null) {
            findNavController().popBackStack()
        }
    }

}