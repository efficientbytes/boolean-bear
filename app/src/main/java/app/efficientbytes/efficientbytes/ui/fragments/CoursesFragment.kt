package app.efficientbytes.efficientbytes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.efficientbytes.R
import app.efficientbytes.efficientbytes.databinding.FragmentCoursesBinding

class CoursesFragment : Fragment() {

    private val tagCoursesFragment: String = "View-Byte-Course-Fragment"
    private lateinit var _binding: FragmentCoursesBinding
    private val binding get() = _binding
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        rootView = binding.root
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_account_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.accountSettingsMenu -> {
                        view.findNavController()
                            .navigate(R.id.coursesFragment_to_accountSettingsFragment)
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)

    }

}