package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentEditProfileFieldBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.VerifyPrimaryEmailAddress
import app.efficientbytes.booleanbear.ui.models.EDIT_PROFILE_FIELD
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.extractUsernameFromGitHubUrl
import app.efficientbytes.booleanbear.utils.extractUsernameFromLinkedInUrl
import app.efficientbytes.booleanbear.utils.isGitHubAddress
import app.efficientbytes.booleanbear.utils.isLinkedInAddress
import app.efficientbytes.booleanbear.utils.validateEmailIdFormat
import app.efficientbytes.booleanbear.utils.validateNameFormat
import app.efficientbytes.booleanbear.viewmodels.EditProfileFieldViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
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
    private val viewModel: EditProfileFieldViewModel by viewModels()
    private val connectivityListener: ConnectivityListener by inject()
    private val safeArgs: EditProfileFieldFragmentArgs by navArgs()
    private var professionsListFailedToLoad = false
    private var toolbar: MaterialToolbar? = null

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

        this.index = safeArgs.index
        val source = safeArgs.source
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (source) {
                    1 -> {
                        //opened via verify primary email
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }

                    2 -> {
                        //opened from edit profile
                        findNavController().popBackStack()
                    }
                }
            }
        }
        // Add the callback to the dispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        if (toolbar == null) {
            toolbar = requireActivity().findViewById(R.id.mainToolbar)
            toolbar?.setTitleTextAppearance(
                requireContext(),
                R.style.DefaultToolbarTitleAppearance
            )
        }

        binding.progressBar.visibility = View.GONE
        binding.progressStatusValueTextView.visibility = View.GONE
        binding.progressLinearLayout.visibility = View.GONE
        var currentValue = ""
        mainViewModel.getProfessionalAdapterList()
        mainViewModel.professionalAdapterList.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.NoInternet -> {
                    professionsListFailedToLoad = true
                }

                DataStatus.Status.Success -> {
                    val professionsAdapterList = it.data
                    if (professionsAdapterList != null) {
                        val currentProfessionCategories =
                            professionsAdapterList.map { item -> item.name }
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

                DataStatus.Status.TimeOut -> {
                    professionsListFailedToLoad = true
                }

                else -> {

                }
            }
        }
        mainViewModel.getFirebaseUserToken()
        mainViewModel.firebaseUserToken.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    it.data?.let { token ->
                        val isEmailVerified = token.claims["emailVerified"]
                        if (isEmailVerified is Boolean) this@EditProfileFieldFragment.emailVerified =
                            isEmailVerified
                        binding.emailVerified = this@EditProfileFieldFragment.emailVerified
                    }
                }

                else -> {

                }
            }
        }
        val fieldType = EDIT_PROFILE_FIELD.getField(index)
        toolbar?.title = fieldType.toolbarTile
        mainViewModel.liveUserProfileFromLocal.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                when (fieldType) {
                    EDIT_PROFILE_FIELD.FIRST_NAME -> {
                        val firstName = it.firstName
                        currentValue = firstName ?: ""
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    EDIT_PROFILE_FIELD.LAST_NAME -> {
                        currentValue = it.lastName.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    EDIT_PROFILE_FIELD.EMAIL_ADDRESS -> {
                        currentValue = it.emailAddress.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                    }

                    EDIT_PROFILE_FIELD.PHONE_NUMBER -> {
                        currentValue = it.completePhoneNumber
                        phoneNumber = currentValue
                        binding.fieldTextInputEditText.setText(currentValue)
                    }

                    EDIT_PROFILE_FIELD.PROFESSION -> {
                        val profession = it.profession
                        currentProfessionCategoryPosition = profession ?: 0
                        binding.saveButton.isEnabled = false
                    }

                    EDIT_PROFILE_FIELD.UNIVERSITY_NAME -> {
                        binding.universityAutoCompleteTextView.setText(it.universityName)
                        binding.saveButton.isEnabled = false
                    }

                    EDIT_PROFILE_FIELD.LINKED_IN_USER_NAME -> {
                        currentValue = it.linkedInUsername.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    EDIT_PROFILE_FIELD.GIT_HUB_USER_NAME -> {
                        currentValue = it.gitHubUsername.orEmpty()
                        binding.fieldTextInputEditText.setText(currentValue)
                        binding.saveButton.isEnabled = false
                    }

                    EDIT_PROFILE_FIELD.DEFAULT -> {

                    }
                }
            }
        }
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
                            getString(
                                R.string.by_verifying_a_new_email_address_will_no_longer_be_associated_with_your_account,
                                currentValue
                            )
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
                        EDIT_PROFILE_FIELD.LINKED_IN_USER_NAME -> linkedInUsername =
                            extractUsernameFromLinkedInUrl(input)

                        EDIT_PROFILE_FIELD.GIT_HUB_USER_NAME -> gitHubUsername =
                            extractUsernameFromGitHubUrl(input)

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
                        VerifyPrimaryEmailAddress(input, profile.firstName)
                    viewModel.sendVerificationLinkToPrimaryEmailAddress(verifyPrimaryEmailAddress)
                }
            }
        }
        viewModel.userProfileServerResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message.toString()
                    binding.saveButton.isEnabled = true
                }

                DataStatus.Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.updating_your_profile)
                    binding.saveButton.isEnabled = false
                }

                DataStatus.Status.Success -> {
                    it.data?.let { userProfile ->
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.profile_has_been_updated)
                        binding.saveButton.isEnabled = false
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.no_internet_connection_please_try_again)
                    binding.saveButton.isEnabled = true
                }

                DataStatus.Status.TimeOut -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.time_out_please_try_again)
                    binding.saveButton.isEnabled = true
                }

                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                    binding.saveButton.isEnabled = true
                }
            }
        }
        binding.currentProfessionAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedProfessionCategoryPosition = position
            if (SingletonUserData.getInstance()?.profession == null) {
                binding.saveButton.isEnabled = true
            } else {
                binding.saveButton.isEnabled =
                    currentProfessionCategoryPosition != selectedProfessionCategoryPosition
            }
        }
        viewModel.primaryEmailAddressVerificationServerStatus.observe(viewLifecycleOwner) {
            it?.let { result ->
                binding.progressLinearLayout.visibility = View.VISIBLE
                when (result.status) {
                    DataStatus.Status.Failed -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text = it.message.toString()
                        binding.verifyButton.isEnabled = true
                        viewModel.resetPrimaryEmailAddress()
                    }

                    DataStatus.Status.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.sending_verification_link)
                        binding.verifyButton.isEnabled = false
                    }

                    DataStatus.Status.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.verification_link_sent_successfully)
                        binding.verifyButton.isEnabled = false
                        val directions =
                            EditProfileFieldFragmentDirections.editProfileFieldFragmentToVerifyPrimaryEmailFragment(
                                2
                            )
                        findNavController().navigate(directions)
                        viewModel.resetPrimaryEmailAddress()
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.no_internet_connection_please_try_again)
                        binding.verifyButton.isEnabled = true
                        viewModel.resetPrimaryEmailAddress()
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.time_out_please_try_again)
                        binding.verifyButton.isEnabled = true
                        viewModel.resetPrimaryEmailAddress()
                    }

                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.progressStatusValueTextView.visibility = View.VISIBLE
                        binding.progressStatusValueTextView.text =
                            getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
                        binding.verifyButton.isEnabled = true
                        viewModel.resetPrimaryEmailAddress()
                    }
                }
            }
        }

        connectivityListener.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    if (professionsListFailedToLoad) {
                        professionsListFailedToLoad = false
                        mainViewModel.getProfessionalAdapterList()
                    }
                }

                false -> {

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
            return if (input.isNotBlank()) {
                if (isLinkedInAddress(input)) {
                    binding.fieldTextInputLayout.error = null
                    return if (extractUsernameFromLinkedInUrl(input) != null) {
                        binding.fieldTextInputLayout.error = null
                        true
                    } else {
                        binding.fieldTextInputLayout.error =
                            "Oops! It seems like the input you provided doesn't match the LinkedIn address format. Please make sure you enter a valid LinkedIn profile URL. The correct format should be something like 'https://www.linkedin.com/in/username'. Please try again."
                        false
                    }
                } else {
                    binding.fieldTextInputLayout.error =
                        "Oops! It seems like the input you provided doesn't match the LinkedIn address format. Please make sure you enter a valid LinkedIn profile URL. The correct format should be something like 'https://www.linkedin.com/in/username'. Please try again."
                    return false
                }
            } else {
                binding.fieldTextInputLayout.error = null
                true
            }
        }
        if (EDIT_PROFILE_FIELD.getField(index) == EDIT_PROFILE_FIELD.GIT_HUB_USER_NAME) {
            return if (input.isNotBlank()) {
                if (isGitHubAddress(input)) {
                    binding.fieldTextInputLayout.error = null
                    return if (extractUsernameFromGitHubUrl(input) != null) {
                        binding.fieldTextInputLayout.error = null
                        true
                    } else {
                        binding.fieldTextInputLayout.error =
                            "Oops! It looks like the input you provided doesn't match the GitHub URL format. Please make sure you enter a valid GitHub profile URL. The correct format should be something like 'https://github.com/username'. Please try again."
                        false
                    }
                } else {
                    binding.fieldTextInputLayout.error =
                        "Oops! It looks like the input you provided doesn't match the GitHub URL format. Please make sure you enter a valid GitHub profile URL. The correct format should be something like 'https://github.com/username'. Please try again."
                    return false
                }
            } else {
                binding.fieldTextInputLayout.error = null
                true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (toolbar == null) {
            toolbar = requireActivity().findViewById(R.id.mainToolbar)
        }
    }

}