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
import app.efficientbytes.booleanbear.databinding.FragmentCompleteProfileBinding
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PhoneNumber
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
    private lateinit var completePhoneNumber: String
    private lateinit var phoneNumber: PhoneNumber
    private lateinit var userAccountId: String
    private var passwordCreated: Boolean = false
    private var selectedProfessionCategoryPosition: Int = 0
    private var currentProfessionCategoryPosition: Int = 0
    private val viewModel: CompleteProfileViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
    private val connectivityListener: ConnectivityListener by inject()
    private var professionsListFailedToLoad = false
    private val safeArgs: CompleteProfileFragmentArgs by navArgs()

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
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        this.phoneNumber = PhoneNumber(safeArgs.prefix, safeArgs.phoneNumber)
        this.completePhoneNumber = safeArgs.prefix + safeArgs.phoneNumber
        this.userAccountId = safeArgs.userAccountId
        this.passwordCreated = safeArgs.passwordCreated

        binding.phoneNumberTextInputLayout.prefixText = safeArgs.prefix
        binding.phoneNumberTextInputEditText.setText(safeArgs.phoneNumber)

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
            currentProfessionCategoryPosition = position
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
                        phoneNumber = phoneNumber.phoneNumber,
                        phoneNumberPrefix = phoneNumber.prefix,
                        completePhoneNumber = phoneNumber.prefix + phoneNumber.phoneNumber,
                        profession = profession
                    )
                )
            }
        }
        viewModel.userProfileServerResponse.observe(viewLifecycleOwner) {
            binding.progressLinearLayout.visibility = View.VISIBLE
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text = it.message
                }

                DataStatus.Status.Loading -> {
                    binding.submitButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.updating_your_profile)
                }

                DataStatus.Status.Success -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    it.data?.let { userProfile ->
                        binding.progressStatusValueTextView.text = it.message.toString()
                        mainViewModel.saveUserProfile(userProfile)
                        if (passwordCreated) {
                            findNavController().popBackStack(R.id.homeFragment, false)
                        } else {
                            val directions =
                                CompleteProfileFragmentDirections.completeProfileFragmentToManagePasswordFragment(
                                    1
                                )
                            findNavController().navigate(directions)
                        }
                    }
                }

                DataStatus.Status.NoInternet -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.no_internet_connection_please_try_again)
                }

                DataStatus.Status.TimeOut -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.time_out_please_try_again)
                }

                else -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        getString(R.string.we_encountered_a_problem_please_try_again_after_some_time)
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