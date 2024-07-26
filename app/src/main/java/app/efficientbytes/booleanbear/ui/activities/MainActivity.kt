package app.efficientbytes.booleanbear.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
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
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.efficientbytes.booleanbear.BuildConfig
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.ActivityMainBinding
import app.efficientbytes.booleanbear.models.AdTemplate
import app.efficientbytes.booleanbear.models.SingletonSingleDeviceLogin
import app.efficientbytes.booleanbear.models.SingletonUserData
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AdFreeSessionService
import app.efficientbytes.booleanbear.utils.AdFreeSessionWorker
import app.efficientbytes.booleanbear.utils.AppAuthStateListener
import app.efficientbytes.booleanbear.utils.ConnectivityListener
import app.efficientbytes.booleanbear.utils.SingleDeviceLoginListener
import app.efficientbytes.booleanbear.utils.UserProfileListener
import app.efficientbytes.booleanbear.utils.compareDeviceId
import app.efficientbytes.booleanbear.viewmodels.MainViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
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
                R.id.reportSubmittedFragment,
                R.id.managePasswordFragment,
                R.id.verifyPrimaryEmailFragment,
                R.id.verifyReporterFragment
            )
        )
    }

    //property injection
    private val viewModel: MainViewModel by viewModel<MainViewModel>()
    private val connectivityListener: ConnectivityListener by inject()
    private val workManager: WorkManager by inject()

    // live data listener
    private val userProfileListener: UserProfileListener by inject()
    private val singleDeviceLoginListener: SingleDeviceLoginListener by inject()
    private val appAuthStateListener: AppAuthStateListener by inject()

    //flags
    private var networkNotAvailable = false
    private var networkNotAvailableAtAppLoading = false
    private var userProfileFailedToLoad = false
    private var singleDeviceLoginFailedToLoad = false
    private var serverTimeFailedToLoad = false
    private var accountDeletionFailed = false
    private var waitingListCoursesFailedToLoad = false
    private var professionalAdapterFailedToLoad = false
    private var issueCategoriesFailedToLoad = false
    private var isDialogOpened: Boolean = false

    //dialog
    private var dialog: Dialog? = null

    //for ad mob rewarded ad
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var rewardedAd: RewardedAd? = null
    private var adsToShow = 0
    private var adsShown = 0
    private var pauseRewardedAdWorkerRequest: OneTimeWorkRequest? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                Snackbar.make(
                    binding.mainCoordinatorLayout,
                    getString(R.string.you_have_denied_the_permission_to_show_notifications_to_show_notifications_you_can_enable_it_in_settings),
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
        var isAdLoading = false
        var isAdTemplateActive = false
        var currentAdTemplate: AdTemplate? = AdTemplate.TEMPLATE_15
        var adPauseOverMessageDisplayed = true
        private val isSingleDeviceLoginLaunched = AtomicBoolean(false)
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
        setUpAdMob()
        processIntent(intent)
    }

    private fun setUpAdMob() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        /* // Set your test devices.
         MobileAds.setRequestConfiguration(
             RequestConfiguration.Builder().setTestDeviceIds(listOf("66A5C81099D40CBA1C639D1FC4181ECC")).build()
         )*/
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@MainActivity) {}
        }
    }

    private fun setUpLiveDataObserver() {
        //user profile process
        viewModel.liveUserProfileFromLocal.observe(this) { userProfile ->
            if (userProfile == null) {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    viewModel.getUserProfileFromRemote()
                }
            } else {
                SingletonUserData.setInstance(userProfile)
            }
        }
        userProfileListener.liveUserProfileFromRemote.observe(this) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.Failed -> {
                        if (it.message?.contains(getString(R.string.permission_denied)) == false) {
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
                        currentUser?.let {
                            viewModel.getFirebaseUserToken()
                            viewModel.getUserProfileFromRemote()
                        }
                    }

                    else -> {

                    }
                }
            }
        }
        userProfileListener.userProfileFromRemote.observe(this) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.NoInternet -> userProfileFailedToLoad = true
                    DataStatus.Status.TimeOut -> userProfileFailedToLoad = true

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

                    else -> {

                    }
                }
            }
        }
        //single device login process
        viewModel.liveSingleDeviceLoginFromLocal.observe(this) { singleDeviceLogin ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && singleDeviceLogin == null) {
                multipleDeviceLoginDetectedDialog()
                viewModel.signOutUser()
            } else if (currentUser != null && singleDeviceLogin != null) {
                SingletonSingleDeviceLogin.setInstance(singleDeviceLogin)
            }
        }
        singleDeviceLoginListener.liveSingleDeviceLoginFromRemote.observe(this) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.Success -> {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLoginFromRemote()
                        }
                    }

                    else -> {

                    }
                }
            }
        }
        singleDeviceLoginListener.singleDeviceLoginFromRemote.observe(this) {
            it?.let {
                when (it.status) {
                    DataStatus.Status.Failed -> {
                        singleDeviceLoginFailedToLoad = true
                    }

                    DataStatus.Status.Success -> {
                        it.data?.let { singleDeviceLoginFromServer ->
                            SingletonSingleDeviceLogin.getInstance()
                                ?.let { singleDeviceLoginFromDB ->
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
        }
        //auth state listener
        appAuthStateListener.liveAuthStateFromRemote.observe(this) {
            when (it) {
                true -> {
                    FirebaseAuth.getInstance().currentUser?.let { user ->
                        viewModel.generateFCMToken()
                        if (!isUserLoggedIn) {
                            isUserLoggedIn = true
                            viewModel.getFirebaseUserToken()
                            viewModel.getLiveUserProfileFromRemote(user.uid)
                            viewModel.getLiveSingleDeviceLoginFromRemote(user.uid)
                        }
                    }
                }

                false -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        isUserLoggedIn = false
                        viewModel.resetUser()
                        viewModel.resetSingleDeviceLogin()
                        viewModel.resetAuth()
                        viewModel.resetAssets()

                        viewModel.resetIDToken()
                        viewModel.resetFCMToken()

                        cancelAdFreeSessionWorker()
                        cancelAdFreeSessionService()
                        viewModel.deleteActiveAdsTemplate()
                    }
                    Toast.makeText(
                        this,
                        getString(R.string.you_have_been_signed_out), Toast.LENGTH_LONG
                    ).show()
                }

                null -> {

                }
            }
        }
        //firebase user token
        viewModel.firebaseUserToken.observe(this) {
            when (it.status) {
                DataStatus.Status.Success -> {
                    it.data?.let { token ->
                        token.token?.let { idToken ->
                            viewModel.saveIDToken(idToken)
                        }
                    }
                }

                else -> {

                }
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
                                .setTitle(getString(R.string.time_not_synced))
                                .setMessage(getString(R.string.your_device_time_is_not_in_sync_please_synchronize_your_device_s_time_to_continue_using_the_app))
                                .setPositiveButton(getString(R.string.ok)) { _, _ ->
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

        viewModel.waitingListCourses.observe(this) {
            when (it.status) {
                DataStatus.Status.NoInternet -> {
                    waitingListCoursesFailedToLoad = true
                }

                else -> {

                }
            }
        }
        viewModel.preLoadRewardedAdRequested.observe(this) {
            when (it) {
                true -> {
                    preloadRewardedAd()
                    viewModel.resetPreLoadRewardedAd()
                }

                else -> {

                }
            }
        }
        viewModel.showRewardedAds.observe(this) {
            when (it) {
                null -> {

                }

                AdTemplate.TEMPLATE_10 -> {
                    adsToShow = it.adsToShow
                    showRewardedAds(adsToShow, it)
                    viewModel.showRewardedAds(null)
                }

                AdTemplate.TEMPLATE_15 -> {
                    adsToShow = it.adsToShow
                    showRewardedAds(adsToShow, it)
                    viewModel.showRewardedAds(null)
                }

                AdTemplate.TEMPLATE_30 -> {
                    adsToShow = it.adsToShow
                    showRewardedAds(adsToShow, it)
                    viewModel.showRewardedAds(null)
                }

                AdTemplate.TEMPLATE_60 -> {
                    adsToShow = it.adsToShow
                    showRewardedAds(adsToShow, it)
                    viewModel.showRewardedAds(null)
                }
            }
        }
        viewModel.getActiveAdTemplate.observe(this) {
            if (it != null) {
                if (!it.isActive) {
                    cancelAdFreeSessionWorker()
                    cancelAdFreeSessionService()
                } else {
                    val template = AdTemplate.getPauseTimeFor(it.templateId)
                    val startTimestamp = it.enabledAt
                    val currentTimeStamp = System.currentTimeMillis()
                    val difference = currentTimeStamp - startTimestamp
                    val checkTimeInMillis = TimeUnit.MINUTES.toMillis(template.pauseTime)
                    if (difference > checkTimeInMillis) {
                        adPauseOverMessageDisplayed = true
                        viewModel.deleteActiveAdsTemplate()
                    } else {
                        adPauseOverMessageDisplayed = false
                        isAdTemplateActive = true
                        currentAdTemplate = AdTemplate.getPauseTimeFor(it.templateId)
                    }
                }
            } else {
                isAdTemplateActive = false
                if (!adPauseOverMessageDisplayed) {
                    adPauseOverMessageDisplayed = true
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        Snackbar.make(
                            binding.parentConstraintLayout,
                            currentAdTemplate?.completionMessage
                                ?: getString(R.string.your_ad_free_period_has_concluded_we_appreciate_your_support),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
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
                        if (serverTimeFailedToLoad) {
                            serverTimeFailedToLoad = false
                            viewModel.fetchServerTime()
                        }
                        if (currentUser != null) {
                            viewModel.getFirebaseUserToken()
                            viewModel.getSingleDeviceLoginFromRemote()
                            if (userProfileFailedToLoad) {
                                userProfileFailedToLoad = false
                                viewModel.getUserProfileFromRemote()
                            }
                            if (singleDeviceLoginFailedToLoad) {
                                singleDeviceLoginFailedToLoad = false
                                viewModel.getSingleDeviceLoginFromRemote()
                            }
                            if (accountDeletionFailed) {
                                accountDeletionFailed = false
                                viewModel.signOutUser()
                            }
                            if (waitingListCoursesFailedToLoad) {
                                waitingListCoursesFailedToLoad = false
                                viewModel.getAllWaitingListCourses()
                            }
                        }
                    }
                    if (networkNotAvailableAtAppLoading) {
                        networkNotAvailableAtAppLoading = false
                        if (serverTimeFailedToLoad) {
                            serverTimeFailedToLoad = false
                            viewModel.fetchServerTime()
                        }
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            viewModel.getSingleDeviceLoginFromRemote()
                            viewModel.getFirebaseUserToken()
                            viewModel.getAllWaitingListCourses()
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

    private fun preloadRewardedAd() {
        if (isAdLoading) return
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        val adUnitId = BuildConfig.AD_MOB_UNIT_ID
        RewardedAd.load(this, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                isAdLoading = false
                viewModel.onPreLoadingRewardedAdStatusChanged(false)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isAdLoading = false
                viewModel.onPreLoadingRewardedAdStatusChanged(true)
            }
        })

    }

    private fun showRewardedAds(count: Int, adTemplate: AdTemplate) {
        if (count <= 0) return
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    rewardedAd = null
                    adsShown = 0
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when ad fails to show.
                    rewardedAd = null
                    adsShown = 0
                    viewModel.insertActiveAdTemplate(adTemplate)
                    viewModel.adDisplayCompleted(false)
                    activateAdFreeSessionWorker(adTemplate)
                    activateAdFreeSessionService(adTemplate)
                    val rewardMessage = getString(
                        R.string.enjoy_ad_free_contents_for_next_minutes,
                        adTemplate.pauseTime.toString()
                    )
                    Toast.makeText(this@MainActivity, rewardMessage, Toast.LENGTH_LONG)
                        .show()
                }

                override fun onAdShowedFullScreenContent() {
                    preloadRewardedAd()
                    // Called when ad is shown.
                    // This is the place to pause any background processes if needed.
                }
            }

            ad.show(this) { _ ->
                adsShown++
                if (adsShown < adsToShow) {
                    preloadRewardedAd()
                    showRewardedAds(count - 1, adTemplate)
                } else {
                    adsShown = 0
                    currentAdTemplate = adTemplate
                    viewModel.insertActiveAdTemplate(adTemplate)
                    viewModel.adDisplayCompleted(true)
                    activateAdFreeSessionWorker(adTemplate)
                    activateAdFreeSessionService(adTemplate)
                    val rewardMessage = getString(
                        R.string.enjoy_ad_free_contents_for_next_minutes,
                        adTemplate.pauseTime.toString()
                    )
                    Toast.makeText(this@MainActivity, rewardMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
        } ?: run {
            adsShown = 0
            viewModel.adDisplayCompleted(false)
        }
    }

    private fun activateAdFreeSessionWorker(adTemplate: AdTemplate) {
        viewModel.deleteActiveAdsTemplate()
        cancelAdFreeSessionWorker()
        pauseRewardedAdWorkerRequest = OneTimeWorkRequestBuilder<AdFreeSessionWorker>()
            .setInitialDelay(adTemplate.pauseTime, TimeUnit.MINUTES)
            .build()
        workManager.enqueue(pauseRewardedAdWorkerRequest!!)
    }

    private fun cancelAdFreeSessionWorker() {
        pauseRewardedAdWorkerRequest?.let {
            workManager.cancelWorkById(it.id)
            pauseRewardedAdWorkerRequest = null
        }
    }

    private fun activateAdFreeSessionService(adTemplate: AdTemplate) {
        Intent(this@MainActivity, AdFreeSessionService::class.java).also {
            it.action = AdFreeSessionService.IntentAction.START_SERVICE.toString()
            it.putExtra(
                AdFreeSessionService.EXTRA_PAUSE_DURATION_IN_MINUTES,
                adTemplate.pauseTime
            )
            it.putExtra(
                AdFreeSessionService.EXTRA_CONCLUSION_MESSAGE,
                adTemplate.completionMessage
            )
            ContextCompat.startForegroundService(this@MainActivity, it)
        }
    }

    private fun cancelAdFreeSessionService() {
        Intent(this, AdFreeSessionService::class.java).also {
            it.action = AdFreeSessionService.IntentAction.STOP_SERVICE.toString()
            startService(it)
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

                R.id.listReelsFragment -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.subtitle = null
                    binding.mainToolbar.setTitleTextAppearance(
                        this,
                        R.style.ListReelsToolbarTitleAppearance
                    )
                    binding.mainToolbar.setSubtitleTextAppearance(
                        this,
                        R.style.ListReelsToolbarSubTitleAppearance
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val navDeepLink = intent.data
            navDeepLink?.let { uri ->
                val navController = findNavController(R.id.fragmentContainer)
                when {
                    uri.toString()
                        .contentEquals("https://app.booleanbear.com/user/account/delete") || uri.toString()
                        .contentEquals("https://app.booleanbear.com/user/account/delete/") -> {
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            viewModel.deleteAccountIntent()
                        } else {
                            Snackbar.make(
                                binding.mainCoordinatorLayout,
                                getString(R.string.you_need_to_be_logged_in_to_delete_your_account),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    uri.toString()
                        .startsWith("https://app.booleanbear.com/watch/content/") -> {
                        val segments = uri.pathSegments
                        if (segments.size == 3) {
                            val reelId = segments.lastOrNull()
                            if (reelId != null) {
                                if (FirebaseAuth.getInstance().currentUser != null) {
                                    viewModel.watchContentIntent(reelId)
                                } else {
                                    Snackbar.make(
                                        binding.mainCoordinatorLayout,
                                        getString(R.string.you_need_to_be_logged_in_to_access_the_content),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }

                    uri.toString().startsWith("https://app.booleanbear.com/binge-watch/topic/") -> {
                        navController.navigate(navDeepLink)
                    }

                    else -> {

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