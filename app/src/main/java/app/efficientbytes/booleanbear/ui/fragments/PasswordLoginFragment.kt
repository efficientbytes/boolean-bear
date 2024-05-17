package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentPasswordLoginBinding
import app.efficientbytes.booleanbear.viewmodels.ManagePasswordViewModel
import org.koin.android.ext.android.inject

class PasswordLoginFragment : Fragment() {

    private lateinit var _binding: FragmentPasswordLoginBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val safeArgs: PasswordLoginFragmentArgs by navArgs()
    private val viewModel: ManagePasswordViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordLoginBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userAccountId = safeArgs.userAccountId

        binding.passwordTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.passwordTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.passwordTextInputEditText.addTextChangedListener(object : TextWatcher {
            private var hasUppercase = false
            private var hasLowercase = false
            private var hasDigit = false
            private var hasSpecialChar = false
            private var hasMinLength = false

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(input: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.passwordTextInputLayout.error = null
                input?.let { text ->
                    // Check for uppercase
                    val newHasUppercase = text.any { it.isUpperCase() }
                    if (newHasUppercase && !hasUppercase) {
                        hasUppercase = true
                        binding.uppercaseChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.md_theme_primaryContainer)
                    } else if (!newHasUppercase && hasUppercase) {
                        hasUppercase = false
                        binding.uppercaseChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.black_1200)
                    }
                    // Check for lowercase
                    val newHasLowercase = text.any { it.isLowerCase() }
                    if (newHasLowercase && !hasLowercase) {
                        hasLowercase = true
                        binding.lowerCaseChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.md_theme_primaryContainer)
                    } else if (!newHasLowercase && hasLowercase) {
                        hasLowercase = false
                        binding.lowerCaseChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.black_1200)
                    }
                    // Check for digit
                    val newHasDigit = text.any { it.isDigit() }
                    if (newHasDigit && !hasDigit) {
                        hasDigit = true
                        binding.numbersChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.md_theme_primaryContainer)
                    } else if (!newHasDigit && hasDigit) {
                        hasDigit = false
                        binding.numbersChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.black_1200)
                    }
                    // Check for special character
                    val specialCharacters = "-$#@_!".toSet()
                    val newHasSpecialChar = text.any { it in specialCharacters }
                    if (newHasSpecialChar && !hasSpecialChar) {
                        hasSpecialChar = true
                        binding.specialCharacterChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.md_theme_primaryContainer)
                    } else if (!newHasSpecialChar && hasSpecialChar) {
                        hasSpecialChar = false
                        binding.specialCharacterChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.black_1200)
                    }
                    // Check for minimum length
                    val newHasMinLength = text.length >= 12
                    if (newHasMinLength && !hasMinLength) {
                        hasMinLength = true
                        binding.characterLimitChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.md_theme_primaryContainer)
                    } else if (!newHasMinLength && hasMinLength) {
                        hasMinLength = false
                        binding.characterLimitChip.chipBackgroundColor =
                            requireContext().getColorStateList(R.color.black_1200)
                    } else {
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.loginButton.setOnClickListener {
            val password = binding.passwordTextInputEditText.text.toString()
            if (validatePassword(password)) {
                //send user account id and password to server
                Toast.makeText(requireContext(), "Password sent to server", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    private fun validatePassword(password: String): Boolean {
        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = "Password is required."
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (password.length < 12) {
            binding.passwordTextInputLayout.error =
                "Password needs to be minimum 12 characters wide."
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (!password.any { it.isUpperCase() }) {
            binding.passwordTextInputLayout.error = "Password needs to have minimum 1 uppercase."
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (!password.any { it.isLowerCase() }) {
            binding.passwordTextInputLayout.error = "Password needs to have minimum 1 lowercase."
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (!password.any { it.isDigit() }) {
            binding.passwordTextInputLayout.error = "Password needs to have minimum 1 digit."
            return false
        }
        binding.passwordTextInputLayout.error = null
        val specialCharacters = "-$#@_!".toSet()
        if (!password.any { it in specialCharacters }) {
            binding.passwordTextInputLayout.error =
                "Password needs to have minimum 1 one special character from : -\$#@_!."
            return false
        }
        binding.passwordTextInputLayout.error = null
        return true
    }

}