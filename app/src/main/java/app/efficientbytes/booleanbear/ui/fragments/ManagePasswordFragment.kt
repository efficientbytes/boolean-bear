package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.databinding.FragmentManagePasswordBinding
import app.efficientbytes.booleanbear.ui.models.PASSWORD_MANAGE_MODE

class ManagePasswordFragment : Fragment() {

    private lateinit var _binding: FragmentManagePasswordBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val safeArgs: ManagePasswordFragmentArgs by navArgs()
    private var mode: PASSWORD_MANAGE_MODE? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagePasswordBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val index = safeArgs.mode
        mode = PASSWORD_MANAGE_MODE.getField(index)

        when (mode) {
            PASSWORD_MANAGE_MODE.CREATE -> {
                binding.managePasswordValueTextView.text = PASSWORD_MANAGE_MODE.CREATE.title
                binding.continueButton.text = PASSWORD_MANAGE_MODE.CREATE.buttonText
                binding.continueButton.setOnClickListener {
                    // create the password
                }
            }

            PASSWORD_MANAGE_MODE.UPDATE -> {
                binding.managePasswordValueTextView.text = PASSWORD_MANAGE_MODE.UPDATE.title
                binding.continueButton.text = PASSWORD_MANAGE_MODE.UPDATE.buttonText
                binding.continueButton.setOnClickListener {
                    // update the password
                }
            }

            else -> {
                findNavController().popBackStack()
            }
        }


    }

}