package app.efficientbytes.booleanbear.ui.activities

import android.content.pm.ActivityInfo
import android.icu.util.Calendar
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.ActivityMainBinding
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.AuthenticationRepository
import app.efficientbytes.booleanbear.repositories.StatisticsRepository
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.ServiceError
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.compareDeviceId
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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
    private val statisticsRepository: StatisticsRepository by inject()
    private var userProfileFailedToLoad = false
    private var singleDeviceLoginFailedToLoad = false
    private var serverTimeFailedToLoad = false
    private var accountDeletionFailed = false

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

                DataStatus.Status.Success -> {
                    it.data?.let { userProfile ->
                        viewModel.saveUserProfile(userProfile)
                    }
                }

                DataStatus.Status.NoInternet -> {
                    userProfileFailedToLoad = true
                }

                DataStatus.Status.TimeOut -> {
                    userProfileFailedToLoad = true
                }

                else -> {

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
                        statisticsRepository.deleteUserScreenTime()
                    }
                }
            }
        }
        userProfileListener.userProfileLiveListener.observe(this) {
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
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        userProfileRepository.getUserProfile(user.uid)
                    }
                    viewModel.getFirebaseUserToken()
                }

                else -> {

                }
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
                    singleDeviceLoginFailedToLoad = true
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

                DataStatus.Status.NoInternet -> {
                    singleDeviceLoginFailedToLoad = true
                }

                DataStatus.Status.TimeOut -> {
                    singleDeviceLoginFailedToLoad = true
                }

                else -> {

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

                DataStatus.Status.EmptyResult -> {}

                DataStatus.Status.NoInternet -> {}

                DataStatus.Status.TimeOut -> {}

                DataStatus.Status.UnAuthorized -> {}

                DataStatus.Status.UnKnownException -> {}
            }
        }
        viewModel.serverTime.observe(this) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    val calendar: Calendar = Calendar.getInstance()
                    val now: Long = calendar.timeInMillis
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

                else -> {

                }
            }
        }
        viewModel.deleteUserAccountStatus.observe(this) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    viewModel.signOutUser()
                }

                DataStatus.Status.NoInternet -> {
                    accountDeletionFailed = true
                }

                DataStatus.Status.TimeOut -> {
                    accountDeletionFailed = true
                }

                else -> {

                }
            }
        }
        binding.retryButton.setOnClickListener {
            if (connectivityListener.isInternetAvailable()) {
                binding.noInternetConstraintLayout.visibility = View.GONE
                binding.mainCoordinatorLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun setupConnectivityListener() {
        connectivityListener.observe(this) { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (networkNotAvailable) {
                        networkNotAvailable = false
                        binding.noInternetConstraintLayout.visibility = View.GONE
                        binding.mainCoordinatorLayout.visibility = View.VISIBLE
                        binding.noInternetLabelTextView.visibility = View.GONE
                        binding.internetIsBackLabelTextView.visibility = View.VISIBLE
                        lifecycleScope.launch {
                            delay(3000)
                            binding.internetIsBackLabelTextView.visibility = View.GONE
                        }
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLogin(currentUser.uid)
                            if (userProfileFailedToLoad) {
                                userProfileFailedToLoad = false
                                userProfileRepository.getUserProfile(
                                    currentUser.uid
                                )
                            }
                            if (singleDeviceLoginFailedToLoad) {
                                singleDeviceLoginFailedToLoad = false
                                viewModel.getSingleDeviceLogin(
                                    currentUser.uid
                                )
                            }
                            if (accountDeletionFailed) {
                                accountDeletionFailed = false
                                viewModel.signOutUser()
                            }
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
                    binding.internetIsBackLabelTextView.visibility = View.GONE
                    binding.noInternetLabelTextView.visibility = View.VISIBLE
                }
            }
        }
        connectivityListener.isInternetAvailable().apply {
            if (!this) {
                binding.noInternetConstraintLayout.visibility = View.VISIBLE
                binding.mainCoordinatorLayout.visibility = View.GONE
                networkNotAvailable = true
            }
        }
    }

    private fun setupNavigation() {
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            when (destination.id) {
                R.id.homeFragment -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    binding.toolbarAppImageView.visibility = View.VISIBLE
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.title = resources.getString(R.string.app_name)
                }

                R.id.shuffledContentPlayerFragment -> {
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    binding.toolbarAppImageView.visibility = View.GONE
                    binding.mainToolbar.visibility = View.GONE
                }

                else -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        statisticsRepository.noteDownScreenClosingTime()
    }

}