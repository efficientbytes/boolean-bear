package app.efficientbytes.androidnow.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import app.efficientbytes.androidnow.R
import app.efficientbytes.androidnow.databinding.ActivityMainBinding
import app.efficientbytes.androidnow.repositories.models.DataStatus
import app.efficientbytes.androidnow.utils.ConnectivityListener
import app.efficientbytes.androidnow.viewmodels.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val tagMainActivity: String = "Main-Activity"
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy {
        findNavController(R.id.fragmentContainer)
    }
    private val appBarConfiguration by lazy {
        AppBarConfiguration(setOf(R.id.coursesFragment, R.id.completeProfileFragment))
    }
    private val connectivityListener: ConnectivityListener by inject()
    private var networkNotAvailable: Boolean = false
    private val viewModel: MainViewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        lifecycle.addObserver(viewModel)
        setSupportActionBar(binding.mainToolbar)
        setupNavigation()
        setupConnectivityListener()
        setUpLiveDataObserver()
    }

    private fun setUpLiveDataObserver() {
        viewModel.userProfile.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    val snackBar = Snackbar.make(
                        binding.mainCoordinatorLayout,
                        "Failed to download latest user profile.",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setBackgroundTint(Color.parseColor("#B00020"))
                    snackBar.setTextColor(Color.parseColor("#FFFFFF"))
                    snackBar.show()
                }

                DataStatus.Status.Loading -> {

                }

                DataStatus.Status.Success -> {
                    it.data?.let { userProfilePayload ->
                        userProfilePayload.userProfile?.let { userProfile ->
                            viewModel.saveUserProfile(userProfile)
                        }
                    }
                }
            }
        }
        viewModel.authState.observe(this) {
            it.let {
                when (it) {
                    true -> {
                        Log.i(tagMainActivity, "User sign in")
                        viewModel.getUserProfile()
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            viewModel.listenToUserProfileChanges(user.uid)
                        }
                    }

                    false -> {
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            viewModel.listenToUserProfileChanges(user.uid)
                        }
                        viewModel.cancelListeningToUserProfileChanges()
                        viewModel.deleteUserProfile()
                        Toast.makeText(this, "You have been signed out.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        viewModel.userProfileLiveDocument.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    Log.i(tagMainActivity, "User profile is null")
                }

                DataStatus.Status.Success -> {
                    Log.i(tagMainActivity, "User profile has been modified")
                    viewModel.getUserProfile()
                    viewModel.getFirebaseUserToken()
                }

                DataStatus.Status.Loading -> {

                }
            }
        }
    }

    private fun setupConnectivityListener() {
        connectivityListener.observe(this) { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (networkNotAvailable) {
                        networkNotAvailable = false
                        val snackBar = Snackbar.make(
                            binding.mainCoordinatorLayout,
                            "Yay! You're back online.", Snackbar.LENGTH_LONG
                        )
                        snackBar.setBackgroundTint(Color.parseColor("#4CAF50"))
                        snackBar.setTextColor(Color.parseColor("#FFFFFF"))
                        snackBar.show()
                    }
                }

                false -> {
                    networkNotAvailable = true
                    val snackBar = Snackbar.make(
                        binding.mainCoordinatorLayout,
                        "Oops! Looks like you're offline.",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setBackgroundTint(Color.parseColor("#B00020"))
                    snackBar.setTextColor(Color.parseColor("#FFFFFF"))
                    snackBar.show()
                }
            }
        }
        connectivityListener.isNetworkAvailable().apply {
            if (!this) {
                networkNotAvailable = true
                val snackBar = Snackbar.make(
                    binding.mainCoordinatorLayout,
                    "Oops! Looks like you're offline.",
                    Snackbar.LENGTH_LONG
                )
                snackBar.setBackgroundTint(Color.parseColor("#B00020"))
                snackBar.setTextColor(Color.parseColor("#FFFFFF"))
                snackBar.show()
            }
        }
    }

    private fun setupNavigation() {
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.coursesFragment -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.title = resources.getString(R.string.app_name)
                    enableToolbarCollapse()
                }

                R.id.accountSettingsFragment -> {
                    preventToolbarCollapse()
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