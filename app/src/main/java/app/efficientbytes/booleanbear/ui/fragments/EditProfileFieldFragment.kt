package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentEditProfileFieldBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.VerifyPrimaryEmailAddress
import app.efficientbytes.booleanbear.ui.models.EDIT_PROFILE_FIELD
import app.efficientbytes.booleanbear.utils.validateEmailIdFormat
import app.efficientbytes.booleanbear.utils.validateNameFormat
import app.efficientbytes.booleanbear.viewmodels.EditProfileFieldViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject

class EditProfileFieldFragment : Fragment() {

    private lateinit var _binding: FragmentEditProfileFieldBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var index = 0
    private var emailVerified = false
    private var phoneNumber = ""
    private var selectedProfessionCategoryPosition: Int = 0
    private var currentProfessionCategoryPosition: Int = 0
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
    private val viewModel: EditProfileFieldViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = EditProfileFieldFragmentArgs.fromBundle(bundle)
        val index = args.index
        this.index = index
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileFieldBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.GONE
        binding.progressLinearLayout.visibility = View.GONE
        var currentValue = ""
        mainViewModel.professionAdapterListFromDB.observe(viewLifecycleOwner) { professionList ->
            professionList?.let {
                val currentProfessionCategories = it.map { item -> item.name }
                val currentProfessionCategoryDropDownAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    currentProfessionCategories
                )
                val userProfile = SingletonUserData.getInstance()?.profession
                userProfile?.let { index ->
                    val profession = currentProfessionCategories[index]
                    binding.currentProfessionAutoCompleteTextView.setText(profession, true)
                }
                binding.currentProfessionAutoCompleteTextView.setAdapter(
                    currentProfessionCategoryDropDownAdapter
                )
            }
        }
        mainViewModel.firebaseUserToken.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {

                }

                DataStatus.Status.Loading -> {

                }

                DataStatus.Status.Success -> {
                    it.data?.let { token ->
                        val isEmailVerified = token.claims["emailVerified"]
                        if (isEmailVerified is Boolean) this@EditProfileFieldFragment.emailVerified =
                            isEmailVerified
                        binding.emailVerified = this@EditProfileFieldFragment.emailVerified
                    }
                }

                DataStatus.Status.EmptyResult -> {}

                DataStatus.Status.NoInternet -> {}

                DataStatus.Status.TimeOut -> {}

                DataStatus.Status.UnAuthorized -> {}

                DataStatus.Status.UnKnownException -> {}
            }
        }
        mainViewModel.listenToUserProfileFromDB.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                SingletonUserData.setInstance(it)
                when (index) {
                    1 -> {
                        currentValue = it.firstName
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    2 -> {
                        currentValue = it.lastName.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    3 -> {
                        currentValue = it.emailAddress.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                    }

                    4 -> {
                        currentValue = it.completePhoneNumber
                        phoneNumber = currentValue
                        binding.fieldTextInputEditText.setText(currentValue)
                    }

                    5 -> {
                        currentProfessionCategoryPosition = it.profession
                        binding.saveButton.isEnabled = false
                    }

                    6 -> {
                        binding.universityAutoCompleteTextView.setText(it.universityName)
                        binding.saveButton.isEnabled = false
                    }

                    7 -> {
                        currentValue = it.linkedInUsername.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    8 -> {
                        currentValue = it.gitHubUsername.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }
                }
            }
        }
        if (index == 0) findNavController().popBackStack(R.id.editProfileFragment, false)
        val fieldType = EDIT_PROFILE_FIELD.getField(index)
        binding.field = fieldType
        binding.emailVerified = false
        if (fieldType == EDIT_PROFILE_FIELD.EMAIL_ADDRESS) {
            binding.verifyButton.isEnabled = !emailVerified
        }
        binding.fieldTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.fieldTextInputLayout.error = null
                val updatedValue = binding.fieldTextInputEditText.text.toString().trim()
                binding.saveButton.isEnabled = updatedValue != currentValue
                if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.EMAIL_ADDRESS) {
                    if (updatedValue != currentValue) {
                        binding.emailVerifiedMessageLabelTextView.visibility = View.GONE
                        binding.emailNotVerifiedMessageLabelTextView.visibility = View.GONE
                        binding.additionalMessageValueTextView.visibility = View.VISIBLE
                        binding.verifyButton.isEnabled = true
                        binding.additionalMessageValueTextView.text =
                            "By verifying a new email address, $currentValue will no longer be associated with your account."
                    } else {
                        binding.additionalMessageValueTextView.visibility = View.GONE
                        if (emailVerified) {
                            binding.emailVerifiedMessageLabelTextView.visibility = View.VISIBLE
                            binding.emailNotVerifiedMessageLabelTextView.visibility = View.GONE
                            binding.verifyButton.isEnabled = false
                        } else {
                            binding.emailVerifiedMessageLabelTextView.visibility = View.GONE
                            binding.emailNotVerifiedMessageLabelTextView.visibility = View.VISIBLE
                            binding.verifyButton.isEnabled = true
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.saveButton.setOnClickListener {
            val input =
                if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.UNIVERSITY_NAME) {
                    binding.universityAutoCompleteTextView.text.toString().trim()
                } else {
                    binding.fieldTextInputEditText.text.toString().trim()
                }
            if (validateInputFormat(index, input)) {
                val userProfile = SingletonUserData.getInstance()
                userProfile?.apply {
                    when (EDIT_PROFILE_FIELD.getField(index)) {
                        EDIT_PROFILE_FIELD.FIRST_NAME -> firstName = input
                        EDIT_PROFILE_FIELD.LAST_NAME -> lastName = input
                        EDIT_PROFILE_FIELD.EMAIL_ADDRESS -> emailAddress = input
                        EDIT_PROFILE_FIELD.PHONE_NUMBER -> completePhoneNumber =
                            this@EditProfileFieldFragment.phoneNumber

                        EDIT_PROFILE_FIELD.PROFESSION -> profession =
                            selectedProfessionCategoryPosition

                        EDIT_PROFILE_FIELD.UNIVERSITY_NAME -> universityName = input
                        EDIT_PROFILE_FIELD.LINKED_IN_USER_NAME -> linkedInUsername = input
                        EDIT_PROFILE_FIELD.GIT_HUB_USER_NAME -> gitHubUsername = input

                        EDIT_PROFILE_FIELD.DEFAULT -> {

                        }
                    }
                    viewModel.updateUserProfile(this)
                }
            }
        }
        binding.verifyButton.setOnClickListener {
            val input = binding.fieldTextInputEditText.text.toString().trim()
            if (validateInputFormat(index, input)) {
                val userProfile = SingletonUserData.getInstance()
                userProfile?.let { profile ->
                    val verifyPrimaryEmailAddress =
                        VerifyPrimaryEmailAddress(input, profile.userAccountId, profile.firstName)
                    viewModel.sendVerificationLinkToPrimaryEmailAddress(verifyPrimaryEmailAddress)
                }

            }
        }
        viewModel.userProfileServerResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message.toString()
                    binding.saveButton.isEnabled = true
                }

                DataStatus.Status.Loading -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Updating your profile..."
                    binding.saveButton.isEnabled = false
                }

                DataStatus.Status.Success -> {
                    it.data?.userProfile?.let { userProfile ->
                        mainViewModel.saveUserProfile(userProfile)
                    }
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Profile has been updated."
                    binding.saveButton.isEnabled = false
                }

                DataStatus.Status.NoInternet -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "No Internet Connection."
                    binding.saveButton.isEnabled = true
                }

                DataStatus.Status.TimeOut -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Updating your profile is taking unusually long time. Please try again."
                    binding.saveButton.isEnabled = true
                }

                else -> {

                }
            }
        }
        binding.currentProfessionAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedProfessionCategoryPosition = position
            binding.saveButton.isEnabled =
                currentProfessionCategoryPosition != selectedProfessionCategoryPosition
        }
        viewModel.primaryEmailAddressVerificationServerStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message.toString()
                    binding.verifyButton.isEnabled = true
                }

                DataStatus.Status.Loading -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Sending verification link..."
                    binding.verifyButton.isEnabled = false
                }

                DataStatus.Status.Success -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Verification link sent successfully.."
                    binding.verifyButton.isEnabled = false
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        com.google.android.material.R.style.MaterialAlertDialog_Material3
                    )
                        .setTitle("Verification link sent")
                        .setMessage("Your verification link has been sent. Please check your email and verify to proceed.")
                        .setPositiveButton("ok") { _, _ ->
                            findNavController().popBackStack(R.id.editProfileFragment, false)
                        }.setCancelable(false)
                        .show()
                }

                DataStatus.Status.NoInternet -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "No Internet Connection."
                    binding.verifyButton.isEnabled = true
                }

                DataStatus.Status.TimeOut -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Sending verification link is taking unusually long time. Please try again."
                    binding.verifyButton.isEnabled = true
                }

                else -> {

                }
            }
        }

    }

    private fun validateInputFormat(index: Int, input: String): Boolean {
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.FIRST_NAME) {
            if (input.isBlank()) {
                binding.fieldTextInputLayout.error = "First name is required."
                return false
            }
            binding.fieldTextInputLayout.error = null
            return validateNameFormat(binding.fieldTextInputLayout, input)
        }
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.LAST_NAME) {
            return if (input.isNotBlank()) validateNameFormat(
                binding.fieldTextInputLayout,
                input
            ) else true
        }
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.EMAIL_ADDRESS) {
            if (input.isBlank()) {
                binding.fieldTextInputLayout.error = "Email address is required."
                return false
            }
            binding.fieldTextInputLayout.error = null
            return validateEmailIdFormat(binding.fieldTextInputLayout, input)
        }
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.PROFESSION) {
            return true
        }
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.LINKED_IN_USER_NAME) {
            return true
        }
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.GIT_HUB_USER_NAME) {
            return true
        }
        return false
    }

}