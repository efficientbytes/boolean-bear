package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.databinding.FragmentPasswordLoginBinding
import app.efficientbytes.booleanbear.viewmodels.ManagePasswordViewModel
import org.koin.android.ext.android.inject

class PasswordLoginFragment : Fragment() {

    private lateinit var _binding: FragmentPasswordLoginBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val safeArgs: PasswordLoginFragmentArgs by navArgs()
    private val viewModel: ManagePasswordViewModel by inject()

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

        binding.loginButton.isEnabled = false
        val userAccountId = safeArgs.userAccountId

        binding.loginButton.setOnClickListener {
            val input = binding.passwordTextInputEditText.text.toString()
            when {
                input.isEmpty() -> {
                    binding.passwordTextInputLayout.error = "Please enter the password"
                    return@setOnClickListener
                }

                input.length < 10 -> {
                    binding.passwordTextInputLayout.error =
                        "Password length does not match the minimum requirement of 10 characters."
                    return@setOnClickListener
                }
            }
            binding.loginButton.isEnabled = false
            //send password to server
        }

        binding.passwordTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.passwordTextInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

}