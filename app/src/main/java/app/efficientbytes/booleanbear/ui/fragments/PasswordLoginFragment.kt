package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentPasswordLoginBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.SignInToken
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import app.efficientbytes.booleanbear.viewmodels.ManagePasswordViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class PasswordLoginFragment : Fragment() {

    private lateinit var _binding: FragmentPasswordLoginBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val safeArgs: PasswordLoginFragmentArgs by navArgs()
    private val viewModel: ManagePasswordViewModel by viewModels()
    private val mainViewModel: MainViewModel by inject()

    //response data
    private lateinit var signInToken: SignInToken

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

        binding.forgotPasswordLinearLayout.visibility = View.GONE
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
                        binding.uppercaseChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_tick_icon
                            )
                    } else if (!newHasUppercase && hasUppercase) {
                        hasUppercase = false
                        binding.uppercaseChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_cross_icon
                            )
                    }
                    // Check for lowercase
                    val newHasLowercase = text.any { it.isLowerCase() }
                    if (newHasLowercase && !hasLowercase) {
                        hasLowercase = true
                        binding.lowerCaseChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_tick_icon
                            )
                    } else if (!newHasLowercase && hasLowercase) {
                        hasLowercase = false
                        binding.lowerCaseChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_cross_icon
                            )
                    }
                    // Check for digit
                    val newHasDigit = text.any { it.isDigit() }
                    if (newHasDigit && !hasDigit) {
                        hasDigit = true
                        binding.numbersChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_tick_icon
                            )
                    } else if (!newHasDigit && hasDigit) {
                        hasDigit = false
                        binding.numbersChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_cross_icon
                            )
                    }
                    // Check for special character
                    val specialCharacters = "$#@_!".toSet()
                    val newHasSpecialChar = text.any { it in specialCharacters }
                    if (newHasSpecialChar && !hasSpecialChar) {
                        hasSpecialChar = true
                        binding.specialCharacterChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_tick_icon
                            )
                    } else if (!newHasSpecialChar && hasSpecialChar) {
                        hasSpecialChar = false
                        binding.specialCharacterChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_cross_icon
                            )
                    }
                    // Check for minimum length
                    val newHasMinLength = text.length >= 12
                    if (newHasMinLength && !hasMinLength) {
                        hasMinLength = true
                        binding.characterLimitChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_tick_icon
                            )
                    } else if (!newHasMinLength && hasMinLength) {
                        hasMinLength = false
                        binding.characterLimitChip.checkedIcon =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.password_cross_icon
                            )
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
                viewModel.authenticateUsingPassword(userAccountId, password)
            }
        }

        binding.tryAnotherWayLabelTextView.setOnClickListener {
            val directions =
                PasswordLoginFragmentDirections.passwordLoginFragmentToOTPVerificationFragment(
                    prefix = safeArgs.prefix,
                    phoneNumber = safeArgs.phoneNumber,
                    forceSendOTP = true,
                    passwordAuthFailed = true,
                )
            findNavController().navigate(directions)
        }

        viewModel.authenticateUser.observe(viewLifecycleOwner) {
            it?.let { status ->
                binding.progressLinearLayout.visibility = View.VISIBLE
                when (status.status) {
                    DataStatus.Status.Failed -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = status.message
                        binding.loginButton.isEnabled = true
                        binding.forgotPasswordLinearLayout.visibility = View.VISIBLE
                        viewModel.resetAuthenticateUserLiveData()
                    }

                    DataStatus.Status.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = getString(R.string.please_wait)
                        binding.loginButton.isEnabled = false
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.no_internet_connection_please_try_again)
                        binding.loginButton.isEnabled = true
                        viewModel.resetAuthenticateUserLiveData()
                    }

                    DataStatus.Status.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.loginButton.isEnabled = false
                        status.data?.let { phoneNumberData ->
                            mainViewModel.getSignInToken(
                                phoneNumberData.prefix,
                                phoneNumberData.phoneNumber
                            )
                        }
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.time_out_please_try_again)
                        binding.loginButton.isEnabled = true
                        viewModel.resetAuthenticateUserLiveData()
                    }

                    DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                        requireContext(),
                        it.message
                    )

                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                        binding.loginButton.isEnabled = true
                        viewModel.resetAuthenticateUserLiveData()
                    }
                }
            }
        }

        mainViewModel.signInToken.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    binding.loginButton.isEnabled = true
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_sign_you_in)
                }

                DataStatus.Status.Success -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    //sign the user with the received sign in token
                    it.data?.let { signInToken ->
                        this@PasswordLoginFragment.signInToken = signInToken
                        mainViewModel.signInWithToken(signInToken)
                        mainViewModel.updatePasswordCreatedFlag(signInToken.passwordCreated)
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.no_internet_connection_please_try_again)
                    binding.loginButton.isEnabled = true
                }

                DataStatus.Status.TimeOut -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.time_out_please_try_again)
                    binding.loginButton.isEnabled = true
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                    binding.loginButton.isEnabled = true
                }
            }
        }

        mainViewModel.isUserSignedIn.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                    binding.loginButton.isEnabled = true
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.please_wait_while_we_sign_you_in)
                }

                DataStatus.Status.Success -> {
                    when (it.data) {
                        true -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.visibility = View.VISIBLE
                            binding.progressStatusValueTextView.text =
                                getString(R.string.you_have_been_signed_in_successfully)
                            mainViewModel.saveSingleDeviceLogin(signInToken.singleDeviceLogin)
                            mainViewModel.getUserProfileFromRemote()
                            mainViewModel.getAllWaitingListCourses()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.signed_in_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            when (signInToken.basicProfileDetailsUpdated) {
                                true -> {
                                    findNavController().popBackStack(
                                        R.id.homeFragment,
                                        false
                                    )
                                }

                                false -> {
                                    val directions =
                                        PasswordLoginFragmentDirections.passwordLoginFragmentToCompleteProfileFragment(
                                            prefix = this@PasswordLoginFragment.signInToken.phoneNumberData.prefix,
                                            phoneNumber = this@PasswordLoginFragment.signInToken.phoneNumberData.phoneNumber,
                                            passwordCreated = this@PasswordLoginFragment.signInToken.passwordCreated,
                                            userAccountId = this@PasswordLoginFragment.signInToken.userAccountId
                                        )
                                    findNavController().navigate(directions)
                                }
                            }
                        }

                        else -> {

                        }
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.no_internet_connection_please_try_again)
                    binding.loginButton.isEnabled = true
                }

                DataStatus.Status.TimeOut -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.time_out_please_try_again)
                    binding.loginButton.isEnabled = true
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                    binding.loginButton.isEnabled = true
                }
            }
        }

    }

    private fun validatePassword(password: String): Boolean {
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
        val specialCharacters = "$#@_!".toSet()
        if (!password.any { it in specialCharacters }) {
            binding.passwordTextInputLayout.error =
                getString(R.string.password_needs_to_have_minimum_1_one_special_character_from)
            return false
        }
        binding.passwordTextInputLayout.error = null
        return true
    }

}