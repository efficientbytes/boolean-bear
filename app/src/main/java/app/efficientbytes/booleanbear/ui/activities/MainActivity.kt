package app.efficientbytes.booleanbear.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import app.efficientbytes.booleanbear.repositories.UtilityDataRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.CustomAuthStateListener
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.compareDeviceId
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
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
    private val statisticsRepository: StatisticsRepository by inject()
    private var userProfileFailedToLoad = false
    private var singleDeviceLoginFailedToLoad = false
    private var serverTimeFailedToLoad = false
    private var accountDeletionFailed = false
    private val utilityDataRepository: UtilityDataRepository by inject()
    private var professionalAdapterFailedToLoad = false
    private var issueCategoriesFailedToLoad = false
    private var dialog: Dialog? = null
    private var isDialogOpened: Boolean = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                Snackbar.make(
                    binding.mainCoordinatorLayout,
                    "You have denied the permission to show notifications. To show notifications you can enable it in settings.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Open Settings") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val settingsIntent: Intent =
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        startActivity(settingsIntent)
                    }
                }.show()
            }
        }

    companion object {

        var isUserLoggedIn = false
        var hasListenedToIntent = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        lifecycle.addObserver(viewModel)
        setSupportActionBar(binding.mainToolbar)
        setupNavigation()
        setupConnectivityListener()
        askNotificationPermission()
        setUpLiveDataObserver()
        processIntent(intent)
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
                    it.message?.let { message ->
                        val snackBar = Snackbar.make(
                            binding.mainCoordinatorLayout,
                            message,
                            Snackbar.LENGTH_LONG
                        )
                        snackBar.show()
                    }
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
                            viewModel.generateFCMToken()
                            if (!isUserLoggedIn) {
                                isUserLoggedIn = true
                                userProfileRepository.getUserProfile()
                                userProfileRepository.listenToUserProfileChange(user.uid)
                                authenticationRepository.listenToSingleDeviceLoginChange(user.uid)
                            }
                        }
                    }

                    false -> {
                        isUserLoggedIn = false
                        viewModel.deleteSingleDeviceLogin()
                        viewModel.deleteFCMToken()
                        viewModel.deleteIDToken()
                        viewModel.deleteUserProfile()
                        userProfileRepository.resetUserProfileScope()
                        authenticationRepository.resetSingleDeviceScope()
                        authenticationRepository.resetAuthScope()
                        Toast.makeText(this, "You have been signed out.", Toast.LENGTH_LONG).show()
                        statisticsRepository.deleteUserScreenTime()
                    }
                }
            }
        }
        userProfileListener.userProfileLiveListener.observe(this) {
            when (it.status) {
                DataStatus.Status.Failed -> {
                    if (it.message?.contains("PERMISSION_DENIED") == false) {
                        val snackBar = Snackbar.make(
                            binding.mainCoordinatorLayout,
                            it.message,
                            Snackbar.LENGTH_INDEFINITE
                        )
                        snackBar.show()
                    }
                }

                DataStatus.Status.Success -> {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        userProfileRepository.getUserProfile()
                        viewModel.getFirebaseUserToken()
                    }
                }

                else -> {

                }
            }
        }
        viewModel.singleDeviceLoginResponseFromDB.observe(this) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            this.singleDeviceLogin = it
            if (currentUser != null && it == null) {
                //show device id does not exist , please login again.
                viewModel.signOutUser()
                multipleDeviceLoginDetectedDialog()
            }
        }
        viewModel.singleDeviceLoginResponseFromServer.observe(this) {
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
                                multipleDeviceLoginDetectedDialog()
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
                        viewModel.getSingleDeviceLogin()
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
                                .setMessage("Your device time is not in sync. Please synchronize your device's time to continue using the app.")
                                .setPositiveButton("ok") { _, _ ->
                                    viewModel.fetchServerTime()
                                }
                                .setCancelable(false)
                                .show()
                        }
                    }
                }

                DataStatus.Status.NoInternet -> {
                    serverTimeFailedToLoad = true
                }

                DataStatus.Status.TimeOut -> {
                    serverTimeFailedToLoad = true
                }

                else -> {

                }
            }
        }
        viewModel.deleteUserAccountStatus.observe(this) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.Success -> {
                        viewModel.signOutUser()
                        viewModel.resetDeleteUserAccountLiveData()
                    }

                    DataStatus.Status.NoInternet -> {
                        accountDeletionFailed = true
                        viewModel.resetDeleteUserAccountLiveData()
                    }

                    DataStatus.Status.TimeOut -> {
                        accountDeletionFailed = true
                        viewModel.resetDeleteUserAccountLiveData()
                    }

                    else -> {
                        viewModel.resetDeleteUserAccountLiveData()
                    }
                }
            }
        }

        viewModel.professionalAdapterList.observe(this) {
            when (it.status) {
                DataStatus.Status.TimeOut -> {
                    professionalAdapterFailedToLoad = true
                }

                DataStatus.Status.NoInternet -> {
                    professionalAdapterFailedToLoad = true
                }

                else -> {

                }
            }
        }

        viewModel.issueCategoriesAdapter.observe(this) {
            when (it.status) {
                DataStatus.Status.TimeOut -> {
                    issueCategoriesFailedToLoad = true
                }

                DataStatus.Status.NoInternet -> {
                    issueCategoriesFailedToLoad = true
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

        viewModel.notificationStatusChanged.observe(this) {
            when (it.status) {
                DataStatus.Status.Success -> {

                }

                else -> {

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
                        binding.noInternetConstraintLayout.visibility = View.GONE
                        binding.mainCoordinatorLayout.visibility = View.VISIBLE
                        binding.noInternetLabelTextView.visibility = View.GONE
                        binding.internetIsBackLabelTextView.visibility = View.VISIBLE
                        lifecycleScope.launch {
                            delay(3000)
                            binding.internetIsBackLabelTextView.visibility = View.GONE
                        }
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (professionalAdapterFailedToLoad) {
                            professionalAdapterFailedToLoad = false
                            viewModel.getProfessionalAdapterList()
                        }
                        if (issueCategoriesFailedToLoad) {
                            issueCategoriesFailedToLoad = false
                            viewModel.getIssueCategoriesAdapterList()
                        }
                        if (serverTimeFailedToLoad) {
                            serverTimeFailedToLoad = false
                            viewModel.fetchServerTime()
                        }
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLogin()
                            if (userProfileFailedToLoad) {
                                userProfileFailedToLoad = false
                                userProfileRepository.getUserProfile()
                            }
                            if (singleDeviceLoginFailedToLoad) {
                                singleDeviceLoginFailedToLoad = false
                                viewModel.getSingleDeviceLogin()
                            }
                            if (accountDeletionFailed) {
                                accountDeletionFailed = false
                                viewModel.signOutUser()
                            }
                        }
                    }
                    if (networkNotAvailableAtAppLoading) {
                        networkNotAvailableAtAppLoading = false
                        if (professionalAdapterFailedToLoad) {
                            professionalAdapterFailedToLoad = false
                            viewModel.getProfessionalAdapterList()
                        }
                        if (issueCategoriesFailedToLoad) {
                            issueCategoriesFailedToLoad = false
                            viewModel.getIssueCategoriesAdapterList()
                        }
                        if (serverTimeFailedToLoad) {
                            serverTimeFailedToLoad = false
                            viewModel.fetchServerTime()
                        }
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLogin()
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
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.mainToolbar.subtitle = null
                    binding.mainToolbar.setTitleTextAppearance(
                        this,
                        R.style.HomeToolbarTitleAppearance
                    )
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.title = resources.getString(R.string.app_name)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                R.id.shuffledContentPlayerFragment -> {
                    binding.mainToolbar.subtitle = null
                    binding.mainToolbar.setTitleTextAppearance(
                        this,
                        R.style.DefaultToolbarTitleAppearance
                    )
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    binding.mainToolbar.visibility = View.GONE
                }

                R.id.discoverFragment -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.subtitle = null
                    binding.mainToolbar.setTitleTextAppearance(
                        this,
                        R.style.DiscoverToolbarTitleAppearance
                    )
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                else -> {
                    binding.mainToolbar.subtitle = null
                    binding.mainToolbar.setTitleTextAppearance(
                        this,
                        R.style.DefaultToolbarTitleAppearance
                    )
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        if (intent != null) {
            if (intent.getStringExtra("redirectLink") != null) {
                val uri: Uri = Uri.parse(intent.getStringExtra("redirectLink"))
                openAppLink(uri)
            } else {
                val extras = intent.extras
                if (extras != null && intent.data == null) {
                    val type = extras.getString("type")
                    if (type == getString(R.string.watch_recommendations)) {
                        val link = extras.getString("redirectLink")
                        openAppLink(Uri.parse(link))
                    }
                } else if (intent.data != null) {
                    val data: Uri = intent.data!!
                    openAppLink(data)
                }

            }
        }
    }

    private fun openAppLink(uri: Uri) {
        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2) {
            val firstSegment = pathSegments[0].orEmpty()
            when {
                firstSegment == "account" -> {
                    val secondSegment = pathSegments[1].orEmpty()
                    when {
                        secondSegment == "delete" -> {
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                viewModel.deleteAccountIntent()
                            } else {
                                val snackBar = Snackbar.make(
                                    binding.mainCoordinatorLayout,
                                    "You need to be logged in to delete your account.",
                                    Snackbar.LENGTH_INDEFINITE
                                )
                                snackBar.show()
                            }
                        }

                        else -> {

                        }
                    }
                }

                else -> {
                    val secondSegment = pathSegments[1].orEmpty()
                    when {
                        secondSegment == "v" -> {
                            val contentId = pathSegments.lastOrNull()
                            if (contentId != null) {
                                viewModel.watchContentIntent(contentId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun multipleDeviceLoginDetectedDialog() {
        if (dialog == null) {
            dialog = Dialog(this)
        }

        if (!isDialogOpened) {
            isDialogOpened = true

            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setContentView(R.layout.dialog_multiple_login_detected)
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.attributes.windowAnimations = R.style.BottomDialogAnimation
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
            val dismissButton = dialog!!.findViewById<MaterialButton>(R.id.dismissButton)

            dialog!!.setOnDismissListener {
                isDialogOpened = false
                dialog = null
            }

            dismissButton.setOnClickListener {
                dialog!!.dismiss()
            }

            dialog!!.show()
        }

    }

}