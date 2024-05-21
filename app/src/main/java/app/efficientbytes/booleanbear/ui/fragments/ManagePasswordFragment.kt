package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentManagePasswordBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.ui.models.PASSWORD_MANAGE_MODE
import app.efficientbytes.booleanbear.viewmodels.ManagePasswordViewModel
import com.google.android.material.appbar.MaterialToolbar
import org.koin.android.ext.android.inject

class ManagePasswordFragment : Fragment() {

    private lateinit var _binding: FragmentManagePasswordBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: ManagePasswordViewModel by inject()
    private val safeArgs: ManagePasswordFragmentArgs by navArgs()
    private lateinit var mode: PASSWORD_MANAGE_MODE
    private var toolbar: MaterialToolbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagePasswordBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val index = safeArgs.mode
        mode = PASSWORD_MANAGE_MODE.getField(index)
        val activity = requireActivity()

        if (toolbar == null) {
            toolbar = activity.findViewById(R.id.mainToolbar)
            toolbar?.setTitleTextAppearance(
                requireContext(),
                R.style.DefaultToolbarTitleAppearance
            )
        }
        // This callback will only be called when MyFragment is at least started.
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                when (mode) {
                    PASSWORD_MANAGE_MODE.CREATE, PASSWORD_MANAGE_MODE.RESET -> {

                    }

                    PASSWORD_MANAGE_MODE.UPDATE -> {
                        // Otherwise, allow the back press to happen normally
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }

                    else -> {

                    }
                }
            }
        }
        // Add the callback to the dispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        when (mode) {
            PASSWORD_MANAGE_MODE.CREATE -> {
                binding.passwordTextInputLayout.hint = PASSWORD_MANAGE_MODE.CREATE.prompt
                binding.continueButton.text = PASSWORD_MANAGE_MODE.CREATE.buttonText
                toolbar?.title = PASSWORD_MANAGE_MODE.CREATE.toolbarTitle
                binding.continueButton.setOnClickListener {
                    val password = binding.passwordTextInputEditText.text.toString()
                    val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString()
                    if (validatePassword(password, confirmPassword)) {
                        viewModel.createAccountPassword(password)
                    }
                }
            }

            PASSWORD_MANAGE_MODE.UPDATE -> {
                binding.passwordTextInputLayout.hint = PASSWORD_MANAGE_MODE.UPDATE.prompt
                binding.continueButton.text = PASSWORD_MANAGE_MODE.UPDATE.buttonText
                toolbar?.title = PASSWORD_MANAGE_MODE.UPDATE.toolbarTitle
                binding.continueButton.setOnClickListener {
                    val password = binding.passwordTextInputEditText.text.toString()
                    val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString()
                    if (validatePassword(password, confirmPassword)) {
                        viewModel.updateAccountPassword(password)
                    }
                }
            }

            PASSWORD_MANAGE_MODE.RESET -> {
                binding.passwordTextInputLayout.hint = PASSWORD_MANAGE_MODE.RESET.prompt
                binding.continueButton.text = PASSWORD_MANAGE_MODE.RESET.buttonText
                toolbar?.title = PASSWORD_MANAGE_MODE.RESET.toolbarTitle
                binding.continueButton.setOnClickListener {
                    val password = binding.passwordTextInputEditText.text.toString()
                    val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString()
                    if (validatePassword(password, confirmPassword)) {
                        viewModel.updateAccountPassword(password)
                    }
                }
            }

            else -> {
                findNavController().popBackStack()
            }
        }

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

        binding.confirmPasswordTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.confirmPasswordTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        viewModel.createPassword.observe(viewLifecycleOwner) {
            it?.let { status ->
                binding.progressLinearLayout.visibility = View.VISIBLE
                when (status.status) {
                    DataStatus.Status.Failed -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = status.message
                        binding.continueButton.isEnabled = true
                        viewModel.resetCreatePasswordLiveData()
                    }

                    DataStatus.Status.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = getString(R.string.please_wait)
                        binding.continueButton.isEnabled = false
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.no_internet_connection_please_try_again)
                        binding.continueButton.isEnabled = true
                        viewModel.resetCreatePasswordLiveData()
                    }

                    DataStatus.Status.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = it.message
                        binding.continueButton.isEnabled = false
                        Toast.makeText(
                            requireContext(),
                            it.message,
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack(R.id.homeFragment, false)
                        viewModel.resetCreatePasswordLiveData()
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.time_out_please_try_again)
                        binding.continueButton.isEnabled = true
                        viewModel.resetCreatePasswordLiveData()
                    }

                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                        binding.continueButton.isEnabled = true
                        viewModel.resetCreatePasswordLiveData()
                    }
                }
            }
        }

        viewModel.updatePassword.observe(viewLifecycleOwner) {
            it?.let { status ->
                binding.progressLinearLayout.visibility = View.VISIBLE
                when (status.status) {
                    DataStatus.Status.Failed -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = status.message
                        binding.continueButton.isEnabled = true
                        viewModel.resetUpdatePasswordLiveData()
                    }

                    DataStatus.Status.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = getString(R.string.please_wait)
                        binding.continueButton.isEnabled = false
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.no_internet_connection_please_try_again)
                        binding.continueButton.isEnabled = true
                        viewModel.resetUpdatePasswordLiveData()
                    }

                    DataStatus.Status.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            it.message
                        binding.continueButton.isEnabled = false
                        Toast.makeText(
                            requireContext(),
                            it.message,
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack(R.id.homeFragment, false)
                        viewModel.resetUpdatePasswordLiveData()
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.time_out_please_try_again)
                        binding.continueButton.isEnabled = true
                        viewModel.resetUpdatePasswordLiveData()
                    }

                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                        binding.continueButton.isEnabled = true
                        viewModel.resetUpdatePasswordLiveData()
                    }
                }
            }
        }

    }

    private fun validatePassword(password: String, confirmPassword: String): Boolean {
        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_is_required)
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (password.length < 12) {
            binding.passwordTextInputLayout.error =
                getString(R.string.password_needs_to_be_minimum_12_characters_wide)
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (!password.any { it.isUpperCase() }) {
            binding.passwordTextInputLayout.error =
                getString(R.string.password_needs_to_have_minimum_1_uppercase)
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (!password.any { it.isLowerCase() }) {
            binding.passwordTextInputLayout.error =
                getString(R.string.password_needs_to_have_minimum_1_lowercase)
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (!password.any { it.isDigit() }) {
            binding.passwordTextInputLayout.error =
                getString(R.string.password_needs_to_have_minimum_1_digit)
            return false
        }
        binding.passwordTextInputLayout.error = null
        val specialCharacters = "-$#@_!".toSet()
        if (!password.any { it in specialCharacters }) {
            binding.passwordTextInputLayout.error =
                getString(R.string.password_needs_to_have_minimum_1_one_special_character_from)
            return false
        }
        binding.passwordTextInputLayout.error = null
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordTextInputLayout.error =
                getString(R.string.please_confirm_your_password)
            return false
        }
        binding.confirmPasswordTextInputLayout.error = null
        if (password != confirmPassword) {
            binding.confirmPasswordTextInputLayout.error =
                getString(R.string.password_does_not_match)
            return false
        }
        binding.confirmPasswordTextInputLayout.error = null
        return true
    }

    override fun onResume() {
        super.onResume()
        if (toolbar == null) {
            toolbar = requireActivity().findViewById(R.id.mainToolbar)
        }
    }

}