package app.efficientbytes.androidnow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.efficientbytes.androidnow.databinding.FragmentReportSubmittedBinding

class ReportSubmittedFragment : Fragment() {

    private val tagEditProfileFieldFragment = "Report-Submitted-Fragment"
    private lateinit var _binding: FragmentReportSubmittedBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var ticketId = ""
    private var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        val args = ReportSubmittedFragmentArgs.fromBundle(bundle)
        val ticketId = args.ticketId
        val message = args.message
        this.ticketId = ticketId
        this.message = message
    }

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
        binding.ticketIdChip.text = "Ticket Id : $ticketId"
        binding.messageLabelTextView.text = message
        binding.goToHomePageButton.setOnClickListener {
            findNavController().popBackStack(R.id.coursesFragment, false)
        }
    }

}