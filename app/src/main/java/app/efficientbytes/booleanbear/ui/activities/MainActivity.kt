package app.efficientbytes.booleanbear.ui.activities

import android.graphics.Color
import android.icu.util.Calendar
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
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.ActivityMainBinding
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.ServiceError
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.compareDeviceId
import app.efficientbytes.booleanbear.utils.formatMillisecondToDateString
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val tagMainActivity: String = "Main-Activity"
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy {
        findNavController(R.id.fragmentContainer)
    }
    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.completeProfileFragment,
                R.id.reportSubmittedFragment
            )
        )
    }
    private val connectivityListener: ConnectivityListener by inject()
    private var networkNotAvailable: Boolean = false
    private var networkNotAvailableAtAppLoading: Boolean = false
    private val viewModel: MainViewModel by viewModel<MainViewModel>()
    private var singleDeviceLogin: SingleDeviceLogin? = null
    private val userProfileListener: UserProfileListener by inject()
    private val singleDeviceLoginListener: SingleDeviceLoginListener by inject()
    private val externalScope: CoroutineScope by inject()
    private val authenticationRepository: AuthenticationRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()
    private val customAuthStateListener: CustomAuthStateListener by inject()
    private val serviceError: ServiceError by inject()

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
        viewModel.listenToUserProfileFromDB.observe(this) { userProfile ->
            userProfile?.let {
                SingletonUserData.setInstance(it)
            }
        }
        userProfileListener.userProfile.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    val snackBar = Snackbar.make(
                        binding.mainCoordinatorLayout,
                        it.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.show()
                }

                DataStatus.Status.Loading -> {

                }

                DataStatus.Status.Success -> {
                    it.data?.let { userProfile ->
                        viewModel.saveUserProfile(userProfile)
                    }
                }
            }
        }
        customAuthStateListener.liveData.observe(this) {
            it.let {
                when (it) {
                    true -> {
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            userProfileRepository.getUserProfile(user.uid)
                            viewModel.getSingleDeviceLogin(user.uid)
                            userProfileRepository.listenToUserProfileChange(user.uid)
                            authenticationRepository.listenToSingleDeviceLoginChange(user.uid)
                        }
                    }

                    false -> {
                        viewModel.deleteSingleDeviceLogin()
                        externalScope.coroutineContext.cancelChildren()
                        viewModel.deleteUserProfile()
                        Toast.makeText(this, "You have been signed out.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        userProfileListener.userProfileListener.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    val snackBar = Snackbar.make(
                        binding.mainCoordinatorLayout,
                        it.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.show()
                }

                DataStatus.Status.Success -> {
                    Log.i(tagMainActivity, "User profile has been modified")
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        userProfileRepository.getUserProfile(user.uid)
                    }
                    viewModel.getFirebaseUserToken()
                }

                DataStatus.Status.Loading -> {

                }
            }
        }
        serviceError.liveData.observe(this) { errorMessage ->
            errorMessage?.let {
                val snackBar = Snackbar.make(
                    binding.mainCoordinatorLayout,
                    it, Snackbar.LENGTH_LONG
                )
                snackBar.show()
            }
        }
        viewModel.singleDeviceLoginFromDB.observe(this) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            this.singleDeviceLogin = it
            if (currentUser != null && it == null) {
                //show device id does not exist , please login again.
                viewModel.signOutUser()
                MaterialAlertDialogBuilder(
                    this@MainActivity,
                    com.google.android.material.R.style.MaterialAlertDialog_Material3
                )
                    .setTitle("Multiple Device Login")
                    .setMessage("We have identified multiple device login associated with this same account. We are logging you out from this device. If you want to use account in this device login again.")
                    .setPositiveButton("ok", null)
                    .setCancelable(false)
                    .show()
            }
        }
        viewModel.singleDeviceLoginFromServer.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    /*//failed to fetch try again
                    FirebaseAuth.getInstance().currentUser?.let { user ->
                        viewModel.getSingleDeviceLogin(user.uid)
                    }*/

                }

                DataStatus.Status.Loading -> {

                }

                DataStatus.Status.Success -> {
                    it.data?.let { singleDeviceLoginFromServer ->
                        this@MainActivity.singleDeviceLogin?.let { singleDeviceLoginFromDB ->
                            if (!compareDeviceId(
                                    singleDeviceLoginFromDB,
                                    singleDeviceLoginFromServer
                                )
                            ) {
                                viewModel.signOutUser()
                                MaterialAlertDialogBuilder(
                                    this@MainActivity,
                                    com.google.android.material.R.style.MaterialAlertDialog_Material3
                                )
                                    .setTitle("Multiple Account Login")
                                    .setMessage("We have detected multiple device logins associated with your account. As a security measure, we are logging you out from this device. If you wish to continue using your account on this device, please log in again.")
                                    .setPositiveButton("ok", null)
                                    .setCancelable(false)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
        singleDeviceLoginListener.liveData.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {

                }

                DataStatus.Status.Success -> {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        viewModel.getSingleDeviceLogin(currentUser.uid)
                    }
                }

                DataStatus.Status.Loading -> {

                }
            }
        }
        viewModel.serverTime.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {

                }

                DataStatus.Status.Loading -> {

                }

                DataStatus.Status.Success -> {
                    val calendar: Calendar = Calendar.getInstance()
                    val now: Long = calendar.timeInMillis
                    Log.i(
                        tagMainActivity, "Server time is ${
                            it.data?.let { it1 ->
                                formatMillisecondToDateString(
                                    it1
                                )
                            }
                        }"
                    )
                    it.data?.let { serverTime ->
                        if ((serverTime - now).absoluteValue > 300000) {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                com.google.android.material.R.style.MaterialAlertDialog_Material3
                            )
                                .setTitle("Time Not Synced")
                                .setMessage("Please synchronize your device's time to continue using the app.")
                                .setPositiveButton("ok") { _, _ ->
                                    viewModel.fetchServerTime()
                                }
                                .setCancelable(false)
                                .show()
                        }
                    }
                }
            }
        }
        viewModel.deleteUserAccountStatus.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {

                }

                DataStatus.Status.Loading -> {

                }

                DataStatus.Status.Success -> {
                    viewModel.signOutUser()
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
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLogin(currentUser.uid)
                        }
                    }
                    if (networkNotAvailableAtAppLoading) {
                        networkNotAvailableAtAppLoading = false
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLogin(currentUser.uid)
                        }
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
        connectivityListener.isInternetAvailable().apply {
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
                R.id.homeFragment -> {
                    binding.toolbarAppImageView.visibility = View.VISIBLE
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.title = resources.getString(R.string.app_name)
                }
                else -> {
                    binding.toolbarAppImageView.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        connectivityListener.isInternetAvailable().apply {
            if (!this) {
                networkNotAvailableAtAppLoading = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        networkNotAvailableAtAppLoading = false
    }
}