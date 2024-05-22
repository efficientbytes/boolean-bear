package app.efficientbytes.booleanbear.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentVerifyPrimaryEmailBinding
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.VerifyPrimaryEmailAddress
import app.efficientbytes.booleanbear.viewmodels.EditProfileFieldViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class VerifyPrimaryEmailFragment : Fragment() {

    private lateinit var _binding: FragmentVerifyPrimaryEmailBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val safeArgs: VerifyPrimaryEmailFragmentArgs by navArgs()
    private val viewModel: EditProfileFieldViewModel by inject()
    private var emailSentMessage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyPrimaryEmailBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val source = safeArgs.source
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (source) {
                    1 -> {
                        //opened from home fragment
                        findNavController().popBackStack()
                    }

                    2 -> {
                        //opened from edit profile field
                        findNavController().popBackStack()
                    }
                }
            }
        }
        // Add the callback to the dispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        when (source) {
            1 -> {
                // if it was opened from home fragment
                emailSentMessage =
                    getString(R.string.we_ve_sent_an_email_to_your_registered_email_address_tap_on_the_verify_button_in_the_email_to_verify_your_account_you_can_change_your_email_here)
                binding.resendEmailLabelTextView.visibility = View.VISIBLE
            }

            2 -> {
                // from edit profile field
                emailSentMessage =
                    getString(R.string.we_ve_sent_an_email_to_your_registered_email_address_tap_on_the_verify_button_in_the_email_to_verify_your_account)
                binding.resendEmailLabelTextView.visibility = View.GONE
            }

            else -> {
                findNavController().popBackStack()
            }
        }
        val emailSentText = emailSentMessage
        val spannableStringBuilder = SpannableStringBuilder(emailSentText)
        if (source == 1) {
            val editProfileText = "here"
            val editProfileStartIndex = emailSentText.indexOf(editProfileText)
            val editProfileEndIndex = editProfileStartIndex + editProfileText.length
            val hereClickSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val directions =
                        VerifyPrimaryEmailFragmentDirections.verifyPrimaryEmailFragmentToEditProfileFieldFragment(
                            3,
                            1
                        )
                    findNavController().navigate(directions)
                }
            }
            if (editProfileStartIndex != -1) {
                spannableStringBuilder.setSpan(
                    hereClickSpan,
                    editProfileStartIndex,
                    editProfileEndIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableStringBuilder.setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.violet_800)),
                    editProfileStartIndex,
                    editProfileEndIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.weJustSentEmailValueTextView.movementMethod =
                LinkMovementMethod.getInstance()
        }
        binding.weJustSentEmailValueTextView.text = spannableStringBuilder

        binding.resendEmailLabelTextView.setOnClickListener {
            val userEmailAddress = SingletonUserData.getInstance()?.emailAddress
            val firstName = SingletonUserData.getInstance()?.firstName

            if (userEmailAddress != null && firstName != null) {
                viewModel.sendVerificationLinkToPrimaryEmailAddress(
                    VerifyPrimaryEmailAddress(
                        userEmailAddress,
                        firstName
                    )
                )
            }
        }

        viewModel.primaryEmailAddressVerificationServerStatus.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    DataStatus.Status.Failed -> {
                        binding.resendEmailLabelTextView.isEnabled = true
                        result.message?.let { message ->
                            Snackbar.make(binding.parentScrollView, message, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        viewModel.resetPrimaryEmailAddress()
                    }

                    DataStatus.Status.Loading -> {
                        binding.resendEmailLabelTextView.isEnabled = false
                    }

                    DataStatus.Status.Success -> {
                        binding.resendEmailLabelTextView.isEnabled = true
                        Snackbar.make(
                            binding.parentScrollView,
                            getString(R.string.verification_link_sent_successfully),
                            Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.resetPrimaryEmailAddress()
                    }

                    DataStatus.Status.NoInternet -> {
                        binding.resendEmailLabelTextView.isEnabled = true
                        Snackbar.make(
                            binding.parentScrollView,
                            R.string.no_internet_connection_please_try_again,
                            Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.resetPrimaryEmailAddress()
                    }

                    DataStatus.Status.TimeOut -> {
                        binding.resendEmailLabelTextView.isEnabled = true
                        Snackbar.make(
                            binding.parentScrollView,
                            R.string.time_out_please_try_again,
                            Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.resetPrimaryEmailAddress()
                    }

                    else -> {
                        binding.resendEmailLabelTextView.isEnabled = true
                        Snackbar.make(
                            binding.parentScrollView,
                            R.string.we_encountered_a_problem_please_try_again_after_some_time,
                            Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.resetPrimaryEmailAddress()
                    }
                }
            }
        }

        binding.openMailAppButton.setOnClickListener {
            // Email app list.
            val emailAppLauncherIntents: MutableList<Intent?> = ArrayList()
            // Create intent which can handle only by email apps.
            val emailAppIntent = Intent(Intent.ACTION_SENDTO)
            emailAppIntent.data = Uri.parse("mailto:")
            // Find from all installed apps that can handle email intent and check version.
            val emailApps = requireContext().packageManager.queryIntentActivities(
                emailAppIntent,
                PackageManager.MATCH_ALL
            )
            // Collect email apps and put in intent list.
            for (resolveInfo in emailApps) {
                val packageName = resolveInfo.activityInfo.packageName
                val launchIntent =
                    requireContext().packageManager.getLaunchIntentForPackage(packageName)
                emailAppLauncherIntents.add(launchIntent)
            }
            // Create chooser with created intent list to show email apps of device.
            val chooserIntent = Intent.createChooser(Intent(), "OPEN EMAIL APP")
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                emailAppLauncherIntents.toTypedArray()
            )
            startActivity(chooserIntent)
        }

    }

}