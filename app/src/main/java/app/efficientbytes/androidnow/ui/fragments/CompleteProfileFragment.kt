package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.FragmentCompleteProfileBinding
import app.efficientbytes.androidnow.models.UserProfile
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.utils.validateEmailIdFormat
import app.efficientbytes.androidnow.utils.validateNameFormat
import app.efficientbytes.androidnow.viewmodels.CompleteProfileViewModel
import org.koin.android.ext.android.inject
import java.util.Locale

class CompleteProfileFragment : Fragment() {

    private val tagCompleteProfileFragment: String = "Complete-Profile-Fragment"
    private lateinit var _binding: FragmentCompleteProfileBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private lateinit var phoneNumber: String
    private var selectedProfessionCategoryPosition: Int = 0
    private val viewModel: CompleteProfileViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = CompleteProfileFragmentArgs.fromBundle(bundle)
        val phoneNumber = args.phoneNumber
        this.phoneNumber = phoneNumber
        val backPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this.isEnabled = false
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneNumberTextInputEditText.setText(phoneNumber)
        val currentProfessionCategories =
            resources.getStringArray(R.array.array_current_profession_categories)
        val currentProfessionCategoryDropDownAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            currentProfessionCategories
        )
        binding.currentProfessionAutoCompleteTextView.setText(currentProfessionCategories[0])
        binding.currentProfessionAutoCompleteTextView.setAdapter(
            currentProfessionCategoryDropDownAdapter
        )
        binding.firstNameTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.firstNameTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.lastNameTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.lastNameTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.emailTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.currentProfessionAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedProfessionCategoryPosition = position
        }
        binding.submitButton.setOnClickListener {
            val firstName = binding.firstNameTextInputEditText.text.toString().trim()
            val lastName = binding.lastNameTextInputEditText.text.toString().trim()
            val emailAddress =
                binding.emailTextInputEditText.text.toString().lowercase(Locale.ROOT).trim()
            val profession = currentProfessionCategories[selectedProfessionCategoryPosition]
            if (validateFormInputFormat(firstName, lastName, emailAddress)) {
                viewModel.updateUserProfile(
                    UserProfile(
                        userAccountId = "aflcI7kP601DQPOyJ1nx",
                        firstName = firstName,
                        lastName = lastName,
                        emailAddress = emailAddress,
                        phoneNumber = phoneNumber,
                        phoneNumberPrefix = "+91",
                        completePhoneNumber = "+91${phoneNumber}",
                        profession = profession
                    )
                )
            }
        }
        viewModel.userProfileServerResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    it.data?.also { userPayLoad ->
                        binding.progressStatusValueTextView.text = userPayLoad.message.toString()
                    }
                }

                DataStatus.Status.Loading -> {
                    binding.submitButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Updating your profile..."
                }

                DataStatus.Status.Success -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    it.data?.also { userPayLoad ->
                        binding.progressStatusValueTextView.text = userPayLoad.message.toString()
                        userPayLoad.userProfile?.also { userProfile ->
                            viewModel.saveUserProfile(userProfile)
                        }
                    }
                    findNavController().popBackStack(R.id.coursesFragment, false)
                }
            }
        }
    }

    private fun validateFormInputFormat(
        firstName: String,
        lastName: String,
        emailAddress: String
    ): Boolean {
        if (firstName.isBlank()) {
            binding.firstNameTextInputLayout.error = "First name is required."
            return false
        }
        binding.firstNameTextInputLayout.error = null
        if (emailAddress.isBlank()) {
            binding.emailTextInputLayout.error = "Email address is required."
            return false
        }
        binding.emailTextInputLayout.error = null
        return validateNameFormat(
            binding.firstNameTextInputLayout,
            firstName
        )
                && validateNameFormat(
            binding.lastNameTextInputLayout,
            lastName
        )
                && validateEmailIdFormat(
            binding.emailTextInputLayout,
            emailAddress
        )
    }
}