package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import app.efficientbytes.booleanbear.BR
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentCoursesBinding
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.database.models.ContentCategory
import app.efficientbytes.booleanbear.ui.adapters.GenericAdapter
import app.efficientbytes.booleanbear.ui.adapters.HomeFragmentChipRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.adapters.InfiniteViewPagerAdapter
import app.efficientbytes.booleanbear.ui.models.CoursesBanner
import app.efficientbytes.booleanbear.viewmodels.CourseViewModel
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class CoursesFragment : Fragment(), HomeFragmentChipRecyclerViewAdapter.OnItemClickListener {

    private val tagCoursesFragment: String = "View-Byte-Course-Fragment"
    private lateinit var _binding: FragmentCoursesBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val viewModel: CourseViewModel by inject()
    private lateinit var infiniteRecyclerAdapter: InfiniteViewPagerAdapter
    private var sampleList: MutableList<CoursesBanner> = mutableListOf()
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 1
    private val DELAY_MS: Long = 3000 // Delay in milliseconds
    private val authenticationRepository: AuthenticationRepository by inject()
    private lateinit var homeFragmentChipRecyclerViewAdapter: HomeFragmentChipRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycle.addObserver(viewModel)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_account_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.accountSettingsMenu -> {
                        val accountSettingsFragment = AccountSettingsFragment()
                        accountSettingsFragment.show(
                            parentFragmentManager,
                            AccountSettingsFragment.tagAccountSettings
                        )
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
        //set content category recycler view
        binding.playlistRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        homeFragmentChipRecyclerViewAdapter =
            HomeFragmentChipRecyclerViewAdapter(emptyList(), requireContext(), this)
        binding.playlistRecyclerView.adapter = homeFragmentChipRecyclerViewAdapter

        viewModel.contentCategoriesFromDB.observe(viewLifecycleOwner) {
            homeFragmentChipRecyclerViewAdapter.setContentCategories(it.toList().subList(0,6))
        }

        //set contents recycler view
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.allShortCourses.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                }

                DataStatus.Status.Loading -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.shimmerLayoutNestedScrollView.visibility = View.VISIBLE
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                }

                DataStatus.Status.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayoutNestedScrollView.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    it.data?.apply {
                        binding.recyclerView.adapter = GenericAdapter(
                            this,
                            R.layout.recycler_view_item_short_courses,
                            BR.course
                        )
                    }
                }
            }
        }
    }

    private fun getSampleData() {
        sampleList.clear()
        val banner1 = CoursesBanner(
            "1",
            "Introduction to Programming",
            "https://example.com/course1",
            "https://firebasestorage.googleapis.com/v0/b/pdf-sync-project.appspot.com/o/PDF_FILES%2Fcoconut%20tree.png?alt=media&token=11e28d02-2bbd-4e65-8333-69483503fc18"
        )
        val banner2 = CoursesBanner(
            "2",
            "Mobile App Development",
            "https://example.com/course2",
            "https://firebasestorage.googleapis.com/v0/b/pdf-sync-project.appspot.com/o/PDF_FILES%2Fgirls%20standing.png?alt=media&token=e51ca378-bfe6-4700-8873-3c2637d30bdb"
        )
        val banner3 = CoursesBanner(
            "3",
            "Data Science Fundamentals",
            null,
            "https://firebasestorage.googleapis.com/v0/b/pdf-sync-project.appspot.com/o/PDF_FILES%2Fsky%20scrapper.png?alt=media&token=07b50caa-8960-4983-aa7e-94c00ba48c20"
        )
        val banner4 = CoursesBanner(
            "4",
            "Web Development Masterclass",
            "https://example.com/course4",
            "https://firebasestorage.googleapis.com/v0/b/pdf-sync-project.appspot.com/o/PDF_FILES%2Fsnow%20mountain.png?alt=media&token=a1c14e20-9716-492d-b191-ccd3ebdee13f"
        )
        sampleList.add(banner1)
        sampleList.add(banner2)
        sampleList.add(banner3)
        sampleList.add(banner4)
    }

    private fun startAutoScroll() {
        val runnable = object : Runnable {
            override fun run() {
                if (currentPage == (binding.infiniteViewPager.adapter?.itemCount ?: 1)) {
                    currentPage = 1
                }
                binding.infiniteViewPager.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, DELAY_MS)
            }
        }
        handler.postDelayed(runnable, DELAY_MS)
    }

    private fun onInfinitePageChangeCallback(listSize: Int) {
        binding.infiniteViewPager.adapter = null
        binding.infiniteViewPager.adapter = infiniteRecyclerAdapter
        binding.infiniteViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (binding.infiniteViewPager.currentItem) {
                        listSize - 1 -> binding.infiniteViewPager.setCurrentItem(
                            1,
                            false
                        )

                        0 -> binding.infiniteViewPager.setCurrentItem(
                            listSize - 2,
                            false
                        )
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != 0 && position != listSize - 1) {
                    currentPage = position
                }
            }
        })
    }

    private fun setupViewPager() {
        getSampleData()
        infiniteRecyclerAdapter = InfiniteViewPagerAdapter(sampleList)
        binding.infiniteViewPager.currentItem = 1
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            authenticationRepository.listenForAuthStateChanges()
        }
        onInfinitePageChangeCallback(sampleList.size + 2)
        startAutoScroll()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onChipItemClick(position: Int, contentCategory: ContentCategory) {
        //request videos under the specific category
    }

    override fun onChipLastItemClicked() {
        //open browse categories fragment
    }

}