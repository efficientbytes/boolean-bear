package app.efficientbytes.androidnow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.efficientbytes.androidnow.databinding.FragmentDescribeIssueBinding

class DescribeIssueFragment : Fragment() {

    private lateinit var _binding: FragmentDescribeIssueBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var selectedIssueCategoryPosition: Int = 0

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

        binding.continueButton.setOnClickListener {
            val titleInput = binding.issueTitleInputEditText.text.toString().trim()
            val descriptionInput = binding.issueDescriptionInputEditText.text.toString().trim()

            if (validateInput(titleInput, descriptionInput)) {

            }

        }

        binding.selectIssueCategoryAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedIssueCategoryPosition = position
        }


    }

    private fun validateInput(titleInput: String, descriptionInput: String): Boolean {
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