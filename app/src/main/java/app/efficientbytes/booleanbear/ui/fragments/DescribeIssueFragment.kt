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
import app.efficientbytes.booleanbear.services.models.RequestSupport
import app.efficientbytes.booleanbear.services.models.SingletonRequestSupport
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import org.koin.android.ext.android.inject

class DescribeIssueFragment : Fragment() {

    private lateinit var _binding: FragmentDescribeIssueBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var selectedIssueCategoryPosition: Int = 0
    private val mainViewModel: MainViewModel by inject()

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

        mainViewModel.issueCategoryAdapterListFromDB.observe(viewLifecycleOwner) { issueCategories ->
            issueCategories?.let {
                val issueCategoryList = it.map { item -> item.name }
                val issueCateCategoryDropDownAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    issueCategoryList
                )
                val defaultOption = issueCategories[0].name
                binding.selectIssueCategoryAutoCompleteTextView.setText(defaultOption, false)
                selectedIssueCategoryPosition = 0
                binding.selectIssueCategoryAutoCompleteTextView.setAdapter(
                    issueCateCategoryDropDownAdapter
                )
            }
        }

        binding.continueButton.setOnClickListener {
            val titleInput = binding.issueTitleInputEditText.text.toString().trim()
            val descriptionInput = binding.issueDescriptionInputEditText.text.toString().trim()

            if (validateInput(titleInput, descriptionInput)) {
                val requestSupport = RequestSupport(
                    title = titleInput,
                    description = descriptionInput,
                    category = selectedIssueCategoryPosition
                )
                SingletonRequestSupport.setInstance(requestSupport)
                findNavController().navigate(R.id.describeIssueFragment_to_reporterPhoneNumberFragment)
            }
        }

        binding.selectIssueCategoryAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedIssueCategoryPosition = position
            binding.selectIssueCategoryInputLayout.error = null
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