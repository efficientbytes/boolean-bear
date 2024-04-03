package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.efficientbytes.booleanbear.databinding.FragmentShareFeedbackBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.viewmodels.ShareFeedbackViewModel
import org.koin.android.ext.android.inject

class ShareFeedbackFragment : Fragment() {

    private lateinit var _binding: FragmentShareFeedbackBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: ShareFeedbackViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareFeedbackBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitButton.setOnClickListener {
            val input = binding.feedbackTextInputEditText.text.toString().trim()
            if (validateInput(input)) {
                val userAccountId = SingletonUserData.getInstance()?.userAccountId
                if (userAccountId != null) {
                    viewModel.uploadFeedback(input, userAccountId)
                }
            }
        }
        viewModel.feedbackUploadStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    val error = it.message
                    binding.progressStatusValueTextView.text = error
                    binding.submitButton.isEnabled = true
                }

                DataStatus.Status.Loading -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    binding.progressStatusValueTextView.text =
                        "Please wait while we submit your feedback..."
                    binding.submitButton.isEnabled = false
                }

                DataStatus.Status.Success -> {
                    binding.progressLinearLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressStatusValueTextView.visibility = View.VISIBLE
                    val message = it.data?.message
                    binding.progressStatusValueTextView.text = message
                    binding.submitButton.isEnabled = true
                }
            }
        }
        binding.feedbackTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.feedbackTextInputLayout.error = null
                binding.progressLinearLayout.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.progressStatusValueTextView.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun validateInput(input: String): Boolean {
        if (input.isBlank()) {
            binding.feedbackTextInputLayout.error = "Empty feedback cannot be submitted."
            return false
        }
        binding.feedbackTextInputLayout.error = null
        return true
    }

}