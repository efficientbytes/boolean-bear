package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentEditProfileBinding
import app.efficientbytes.booleanbear.utils.AppAuthStateListener
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject

class EditProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var _binding: FragmentEditProfileBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
    private val appAuthStateListener: AppAuthStateListener by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainViewModel = mainViewModel
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appAuthStateListener.liveAuthStateFromRemote.observe(viewLifecycleOwner) {
            it?.let { authState ->
                when (authState) {
                    true -> {

                    }

                    false -> {
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }
            }
        }
        binding.firstNameLabelTextView.setOnClickListener(this)
        binding.lastNameLabelTextView.setOnClickListener(this)
        binding.emailAddressLabelTextView.setOnClickListener(this)
        binding.phoneNumberLabelTextView.setOnClickListener(this)
        binding.professionLabelTextView.setOnClickListener(this)
        binding.linkedInUsernameLabelTextView.setOnClickListener(this)
        binding.githubUsernameLabelTextView.setOnClickListener(this)
        binding.updatePasswordLabelTextView.setOnClickListener(this)
        binding.signOutLabelTextView.setOnClickListener {
            MaterialAlertDialogBuilder(
                requireContext(),
                com.google.android.material.R.style.MaterialAlertDialog_Material3
            )
                .setTitle("Sign out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign out") { _, _ ->
                    mainViewModel.signOutUser()
                }.setNegativeButton("cancel", null)
                .setCancelable(true)
                .show()
        }
    }

    override fun onClick(view: View?) {
        val id = view?.id
        id?.let {
            val directions = when (it) {
                R.id.firstNameLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(1)
                }

                R.id.lastNameLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(2)
                }

                R.id.emailAddressLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(3)
                }

                R.id.phoneNumberLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(4)
                }

                R.id.professionLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(5)
                }

                R.id.linkedInUsernameLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(7)
                }

                R.id.githubUsernameLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(8)
                }

                R.id.updatePasswordLabelTextView -> {
                    EditProfileFragmentDirections.editProfileFragmentToManagePasswordFragment(2)
                }

                else -> {
                    EditProfileFragmentDirections.editProfileFragmentToEditProfileFieldFragment(0)
                }
            }
            rootView.findNavController().navigate(directions)
        }
    }

}