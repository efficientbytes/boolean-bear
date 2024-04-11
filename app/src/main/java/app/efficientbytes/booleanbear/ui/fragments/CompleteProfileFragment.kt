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
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentCompleteProfileBinding
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.validateEmailIdFormat
import app.efficientbytes.booleanbear.utils.validateNameFormat
import app.efficientbytes.booleanbear.viewmodels.CompleteProfileViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import org.koin.android.ext.android.inject
import java.util.Locale

class CompleteProfileFragment : Fragment() {

    private lateinit var _binding: FragmentCompleteProfileBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private lateinit var phoneNumber: String
    private lateinit var userAccountId: String
    private var selectedProfessionCategoryPosition: Int = 0
    private var currentProfessionCategoryPosition: Int = 0
    private val viewModel: CompleteProfileViewModel by inject()
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
    private val connectivityListener: ConnectivityListener by inject()
    private var professionsListFailedToLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = CompleteProfileFragmentArgs.fromBundle(bundle)
        val phoneNumber = args.phoneNumber
        val userAccountId = args.userAccountId
        this.phoneNumber = phoneNumber
        this.userAccountId = userAccountId
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
        mainViewModel.getProfessionalAdapterList()
        mainViewModel.professionalAdapterList.observe(viewLifecycleOwner) { it ->
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
                        val profession =
                            currentProfessionCategories[currentProfessionCategoryPosition]
                        binding.currentProfessionAutoCompleteTextView.setText(profession, true)

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
            val profession = selectedProfessionCategoryPosition
            if (validateFormInputFormat(firstName, lastName, emailAddress)) {
                viewModel.updateUserPrivateProfileBasicDetails(
                    UserProfile(
                        userAccountId = userAccountId,
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
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    it.data?.also { userPayLoad ->
                        binding.progressStatusValueTextView.text = userPayLoad.message.toString()
                    }
                }

                DataStatus.Status.Loading -> {
                    binding.submitButton.isEnabled = false
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "Updating your profile..."
                }

                DataStatus.Status.Success -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    it.data?.also { userPayLoad ->
                        binding.progressStatusValueTextView.text = userPayLoad.message.toString()
                        userPayLoad.userProfile?.also { userProfile ->
                            mainViewModel.saveUserProfile(userProfile)
                        }
                    }
                    findNavController().popBackStack(R.id.homeFragment, false)
                }

                DataStatus.Status.NoInternet -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = "No Internet Connection."
                }

                DataStatus.Status.TimeOut -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Updating profile is taking unsually long time. Please try submitting it again."
                }

                else -> {

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