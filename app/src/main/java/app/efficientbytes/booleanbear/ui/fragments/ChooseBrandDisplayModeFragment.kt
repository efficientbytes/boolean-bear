package app.efficientbytes.booleanbear.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.databinding.FragmentChooseBrandDisplayModeBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChooseBrandDisplayModeFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentChooseBrandDisplayModeBinding
    private val binding get() = _binding
    private lateinit var rootView: View

    companion object {

        var isOpened = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseBrandDisplayModeBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.onlyLogoTextViewLabel.setOnClickListener {
            val directions =
                ChooseBrandDisplayModeFragmentDirections.chooseBrandDisplayModeFragmentToBrandDisplayFragment(
                    0
                )
            findNavController().navigate(directions)
            dismiss()
        }

        binding.onlyNameTextViewLabel.setOnClickListener {
            val directions =
                ChooseBrandDisplayModeFragmentDirections.chooseBrandDisplayModeFragmentToBrandDisplayFragment(
                    1
                )
            findNavController().navigate(directions)
            dismiss()
        }

        binding.logoNNameTextViewLabel.setOnClickListener {
            val directions =
                ChooseBrandDisplayModeFragmentDirections.chooseBrandDisplayModeFragmentToBrandDisplayFragment(
                    2
                )
            findNavController().navigate(directions)
            dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isOpened = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpened = false
    }

    override fun onDetach() {
        super.onDetach()
        isOpened = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOpened = false
    }

}