package app.efficientbytes.efficientbytes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import app.efficientbytes.efficientbytes.R
import app.efficientbytes.efficientbytes.databinding.ChipCoursesFilterBinding
import app.efficientbytes.efficientbytes.databinding.FragmentCoursesBinding

class CoursesFragment : Fragment() {

    private val tagCoursesFragment: String = "View-Byte-Course-Fragment"
    private lateinit var _binding: FragmentCoursesBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var coursesFilterIdMapping: HashMap<Int, String>? = null

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
        populateChipGroup()
        binding.coursesContentTypeFilterChipGroup.setOnCheckedStateChangeListener { group, _ ->
            val contentType = coursesFilterIdMapping?.get(group.checkedChipId)
            Toast.makeText(
                requireContext(),
                "The content type is : $contentType",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun populateChipGroup() {
        val inflater = LayoutInflater.from(requireContext())
        val filterList = resources.getStringArray(R.array.array_course_filters)
        coursesFilterIdMapping = HashMap()
        var id = 1
        filterList.forEach {
            val chipCoursesFilterBinding: ChipCoursesFilterBinding =
                ChipCoursesFilterBinding.inflate(inflater)
            chipCoursesFilterBinding.name = it
            chipCoursesFilterBinding.isChecked = id == 1
            coursesFilterIdMapping?.set(id, it)
            chipCoursesFilterBinding.root.id = id++
            binding.coursesContentTypeFilterChipGroup.addView(chipCoursesFilterBinding.root)
        }
    }

}