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
import app.efficientbytes.booleanbear.services.models.DeleteUserAccount
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeleteUserAccountFragment : Fragment() {

    private val tagEditProfileFieldFragment = "Delete-User-Account-Fragment"
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
                            mainViewModel.deleteUserAccount(DeleteUserAccount(it.userAccountId))
                        }
                    }
                }
            }
        }
        mainViewModel.deleteUserAccountStatus.observe(viewLifecycleOwner) {
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
                        "Please wait while we process your request."
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
            }
        }
        binding.takeMeToHomePageButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

    }

}