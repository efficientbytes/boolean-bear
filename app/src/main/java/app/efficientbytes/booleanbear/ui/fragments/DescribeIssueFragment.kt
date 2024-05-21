package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentDescribeIssueBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.SingletonRequestSupport
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import org.koin.android.ext.android.inject

class DescribeIssueFragment : Fragment() {

    private lateinit var _binding: FragmentDescribeIssueBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var selectedIssueCategoryPosition: Int = 0
    private val mainViewModel: MainViewModel by inject()
    private val connectivityListener: ConnectivityListener by inject()
    private var issueCategoriesFailedToLoad = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDescribeIssueBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.getIssueCategoriesAdapterList()
        mainViewModel.issueCategoriesAdapter.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.NoInternet -> {
                    issueCategoriesFailedToLoad = true
                }

                DataStatus.Status.Success -> {
                    val issueCategoriesAdapterList = it.data
                    if (issueCategoriesAdapterList != null) {
                        val issueCategories = issueCategoriesAdapterList.map { item -> item.name }
                        val issueCateCategoryDropDownAdapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_item,
                            issueCategories
                        )
                        val defaultOption = issueCategoriesAdapterList[0].name
                        binding.selectIssueCategoryAutoCompleteTextView.setText(
                            defaultOption,
                            false
                        )
                        selectedIssueCategoryPosition = 0
                        binding.selectIssueCategoryAutoCompleteTextView.setAdapter(
                            issueCateCategoryDropDownAdapter
                        )
                    }
                }

                DataStatus.Status.TimeOut -> {
                    issueCategoriesFailedToLoad = true
                }

                else -> {

                }
            }
        }

        binding.continueButton.setOnClickListener {
            val titleInput = binding.issueTitleInputEditText.text.toString().trim()
            val descriptionInput = binding.issueDescriptionInputEditText.text.toString().trim()

            if (validateInput(titleInput, descriptionInput)) {
                val requestSupport = RequestSupport(
                    title = titleInput,
                    description = descriptionInput,
                    category = selectedIssueCategoryPosition,
                    prefix = "",
                    phoneNumber = ""
                )
                SingletonRequestSupport.setInstance(requestSupport)
                findNavController().navigate(R.id.describeIssueFragment_to_reporterPhoneNumberFragment)
            }
        }

        binding.selectIssueCategoryAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedIssueCategoryPosition = position
            binding.selectIssueCategoryInputLayout.error = null
        }

        connectivityListener.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    if (issueCategoriesFailedToLoad) {
                        issueCategoriesFailedToLoad = false
                        mainViewModel.getIssueCategoriesAdapterList()
                    }
                }

                false -> {

                }
            }
        }

    }

    private fun validateInput(titleInput: String, descriptionInput: String): Boolean {
        if (selectedIssueCategoryPosition == 0) {
            binding.selectIssueCategoryInputLayout.error = "Select category."
            return false
        }
        binding.selectIssueCategoryInputLayout.error = null
        if (titleInput.isBlank()) {
            binding.issueTitleInputLayout.error = "Title is required."
            return false
        }
        binding.issueTitleInputLayout.error = null
        if (descriptionInput.isBlank()) {
            binding.issueDescriptionInputLayout.error = "Description is required."
            return false
        }
        binding.issueDescriptionInputLayout.error = null
        return true
    }
}