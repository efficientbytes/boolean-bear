package app.efficientbytes.efficientbytes.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.viewpager2.widget.ViewPager2
import app.efficientbytes.efficientbytes.R
import app.efficientbytes.efficientbytes.databinding.ActivityMainBinding
import app.efficientbytes.efficientbytes.ui.adapters.InfiniteViewPagerAdapter
import app.efficientbytes.efficientbytes.ui.models.CoursesBanner
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val tagMainActivity: String = "Main-Activity"
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy {
        findNavController(R.id.fragmentContainer)
    }
    private val appBarConfiguration by lazy {
        AppBarConfiguration(setOf(R.id.coursesFragment))
    }
    private lateinit var infiniteRecyclerAdapter: InfiniteViewPagerAdapter
    private var sampleList: MutableList<CoursesBanner> = mutableListOf()
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 1
    private val DELAY_MS: Long = 3000 // Delay in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.mainToolbar)
        setupNavigation()
        setupViewPager()
    }

    private fun setupViewPager() {
        getSampleData()
        infiniteRecyclerAdapter = InfiniteViewPagerAdapter(sampleList)
        binding.mainInfiniteViewPager.currentItem = 1
    }

    private fun setupNavigation() {
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.coursesFragment -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.title = resources.getString(R.string.app_name)
                    enableToolbarCollapse()
                    binding.mainInfiniteViewPager.visibility = View.VISIBLE
                }

                R.id.accountSettingsFragment -> {
                    preventToolbarCollapse()
                    binding.mainInfiniteViewPager.visibility = View.GONE
                }

                else -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    private fun preventToolbarCollapse() {
        val behaviour = getAppBarBehaviour()
        behaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
    }

    private fun enableToolbarCollapse() {
        val behaviour = getAppBarBehaviour()
        behaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return true
            }
        })
    }

    private fun getAppBarBehaviour(): AppBarLayout.Behavior {
        val params = binding.mainAppBar.layoutParams as CoordinatorLayout.LayoutParams
        if (params.behavior == null)
            params.behavior = AppBarLayout.Behavior()
        return params.behavior as AppBarLayout.Behavior
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
                if (currentPage == (binding.mainInfiniteViewPager.adapter?.itemCount ?: 1)) {
                    currentPage = 1
                }
                binding.mainInfiniteViewPager.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, DELAY_MS)
            }
        }
        handler.postDelayed(runnable, DELAY_MS)
    }

    private fun onInfinitePageChangeCallback(listSize: Int) {
        binding.mainInfiniteViewPager.adapter = null
        binding.mainInfiniteViewPager.adapter = infiniteRecyclerAdapter
        binding.mainInfiniteViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (binding.mainInfiniteViewPager.currentItem) {
                        listSize - 1 -> binding.mainInfiniteViewPager.setCurrentItem(
                            1,
                            false
                        )

                        0 -> binding.mainInfiniteViewPager.setCurrentItem(
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

    override fun onStart() {
        super.onStart()
        onInfinitePageChangeCallback(sampleList.size + 2)
        startAutoScroll()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }
}