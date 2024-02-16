package app.efficientbytes.efficientbytes.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import app.efficientbytes.efficientbytes.R
import app.efficientbytes.efficientbytes.databinding.ActivityMainBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.mainToolbar)
        setupNavigation()
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
}