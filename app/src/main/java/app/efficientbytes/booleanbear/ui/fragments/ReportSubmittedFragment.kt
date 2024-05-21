package app.efficientbytes.booleanbear.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentReportSubmittedBinding

class ReportSubmittedFragment : Fragment() {

    private lateinit var _binding: FragmentReportSubmittedBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val safeArgs: ReportSubmittedFragmentArgs by navArgs()
    private lateinit var ticketId: String
    private lateinit var message: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportSubmittedBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // This callback will only be called when MyFragment is at least started.
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event

            }
        }
        // Add the callback to the dispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        this.ticketId = safeArgs.ticketId
        this.message = safeArgs.message

        binding.ticketIdChip.text = getString(R.string.ticket_id, ticketId)
        binding.messageLabelTextView.text = message

        binding.ticketIdChip.setOnClickListener {
            val clip: ClipData =
                ClipData.newPlainText(getString(R.string.boolean_bear_ticket_id), ticketId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                requireContext(),
                getString(R.string.copied_ticket_id),
                Toast.LENGTH_LONG
            ).show()
        }

        binding.goToHomePageButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

}