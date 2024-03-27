package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.DeleteAccountConfirmationBinding
import app.efficientbytes.androidnow.databinding.FragmentDeleteUserAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteUserAccountFragment : Fragment() {

    private val tagEditProfileFieldFragment = "Delete-User-Account-Fragment"
    private lateinit var _binding: FragmentDeleteUserAccountBinding
    private val binding get() = _binding
    private lateinit var rootView: View

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
            val dialogBinding: DeleteAccountConfirmationBinding =
                DeleteAccountConfirmationBinding.inflate(layoutInflater, null, false)
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
                    }
                }
            }
        }
    }

}