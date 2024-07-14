package app.efficientbytes.booleanbear.utils

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.util.AttributeSet
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import app.efficientbytes.booleanbear.database.dao.AuthenticationDao
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.ReelDetails
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.appcheck.AppCheckToken
import com.google.firebase.appcheck.appCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.UnknownHostException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun formatTimestampToDateString(timestampInSeconds: Long): String {
    val date = Date(timestampInSeconds * 1000) // Convert seconds to milliseconds
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

fun formatPriceToINR(price: Int): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.currency = Currency.getInstance("INR")
    return currencyFormat.format(price).removePrefix("₹")
}

fun getTimeAgo(timestamp: Long): String {
    val inputInMillis = timestamp * 1000
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - inputInMillis
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24
    val weeksInMilli = daysInMilli * 7
    val elapsedYears = timeDifference / (daysInMilli * 365)
    val elapsedMonths = timeDifference / (daysInMilli * 30)
    val elapsedWeeks = timeDifference / weeksInMilli
    val elapsedDays = timeDifference / daysInMilli
    val elapsedHours = timeDifference / hoursInMilli
    val elapsedMinutes = timeDifference / minutesInMilli

    return when {
        elapsedYears >= 1 -> pluralize(elapsedYears, "year")
        elapsedMonths >= 1 -> pluralize(elapsedMonths, "month")
        elapsedWeeks >= 1 -> pluralize(elapsedWeeks, "week")
        elapsedDays >= 1 -> pluralize(elapsedDays, "day")
        elapsedHours >= 1 -> pluralize(elapsedHours, "hour")
        elapsedMinutes >= 1 -> pluralize(elapsedMinutes, "minute")
        else -> "Just now"
    }
}

fun pluralize(value: Long, unit: String): String {
    return if (value == 1L) {
        "$value $unit ago"
    } else {
        "$value ${unit}s ago"
    }
}

fun validatePhoneNumberFormat(
    phoneNumberTextInputLayout: TextInputLayout,
    input: String?
): Boolean {
    if (input.isNullOrBlank()) {
        phoneNumberTextInputLayout.error = "Please enter phone number to continue."
        return false
    }
    /*phoneNumberTextInputLayout.error = null
     if (!input.matches(Regex("^[1-9]\\d{9}$"))) {
         phoneNumberTextInputLayout.error = "Invalid phone number format."
         return false
     }
     phoneNumberTextInputLayout.error = null*/
    return true
}

fun validateOTPFormat(input: String): Boolean {
    return input.matches(Regex("""^\d{6}$"""))
}

fun validateEmailIdFormat(
    inputLayout: TextInputLayout,
    input: String?
): Boolean {
    if (input?.matches(Regex("""^[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""")) == false) {
        inputLayout.error = "Invalid format"
        return false
    }
    inputLayout.error = null
    return true
}

fun validateNameFormat(
    inputLayout: TextInputLayout,
    input: String?
): Boolean {
    if (input?.isBlank() == true) return true
    if (input?.matches(Regex("""^[a-zA-Z\s]+$""")) == false) {
        inputLayout.error = "Invalid format"
        return false
    }
    inputLayout.error = null
    return true
}

fun compareDeviceId(
    singleDeviceLoginFromDB: SingleDeviceLogin,
    singleDeviceLoginFromServer: SingleDeviceLogin
): Boolean {
    return singleDeviceLoginFromDB.deviceId == singleDeviceLoginFromServer.deviceId && singleDeviceLoginFromDB.createdOn == singleDeviceLoginFromServer.createdOn
}

fun formatMillisecondToDateString(timestampInMillisecond: Long): String {
    val date = Date(timestampInMillisecond)
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

object UserProfileListener {

    //from server (snapshot listener)
    private val _liveUserProfileFromRemote: MutableLiveData<DataStatus<DocumentSnapshot?>?> =
        MutableLiveData()
    val liveUserProfileFromRemote: LiveData<DataStatus<DocumentSnapshot?>?> =
        _liveUserProfileFromRemote

    //from server
    private val _userProfileFromRemote: MutableLiveData<DataStatus<UserProfile>?> =
        MutableLiveData()
    val userProfileFromRemote: LiveData<DataStatus<UserProfile>?> = _userProfileFromRemote

    fun updateLiveUserProfileFromRemote(latestValue: DataStatus<DocumentSnapshot?>?) {
        _liveUserProfileFromRemote.postValue(latestValue)
    }

    fun updateUserProfileFromRemote(value: DataStatus<UserProfile>?) {
        _userProfileFromRemote.postValue(value)
    }

    fun resetAll() {
        _liveUserProfileFromRemote.postValue(null)
        _userProfileFromRemote.postValue(null)
    }

}

object SingleDeviceLoginListener {

    //from server (snapshot listener)
    private val _liveSingleDeviceLoginFromRemote: MutableLiveData<DataStatus<DocumentSnapshot?>?> =
        MutableLiveData()
    val liveSingleDeviceLoginFromRemote: LiveData<DataStatus<DocumentSnapshot?>?> =
        _liveSingleDeviceLoginFromRemote

    //from server
    private val _singleDeviceLoginFromRemote: MutableLiveData<DataStatus<SingleDeviceLogin>?> =
        MutableLiveData()
    val singleDeviceLoginFromRemote: LiveData<DataStatus<SingleDeviceLogin>?> =
        _singleDeviceLoginFromRemote

    fun updateLiveSingleDeviceLoginFromRemote(latestValue: DataStatus<DocumentSnapshot?>?) {
        _liveSingleDeviceLoginFromRemote.postValue(latestValue)
    }

    fun updateSingleDeviceLoginFromRemote(value: DataStatus<SingleDeviceLogin>?) {
        _singleDeviceLoginFromRemote.postValue(value)
    }

    fun resetAll() {
        _liveSingleDeviceLoginFromRemote.postValue(null)
        _singleDeviceLoginFromRemote.postValue(null)
    }

}

object AuthStateCoroutineScope {

    private val handler = CoroutineExceptionHandler { _, exception ->
    }
    private var scope: CoroutineScope? = null

    fun scopeStatus(): CoroutineScope? {
        return scope
    }

    fun resetScope() {
        scope?.coroutineContext?.cancelChildren()
        scope = null
    }

    fun getScope(): CoroutineScope {
        return scope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
    }
}

object UserAccountCoroutineScope {

    private val handler = CoroutineExceptionHandler { _, exception ->
    }
    private var scope: CoroutineScope? = null

    fun scopeStatus(): CoroutineScope? {
        return scope
    }

    fun resetScope() {
        scope?.coroutineContext?.cancelChildren()
        scope = null
    }

    fun getScope(): CoroutineScope {
        return scope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
    }
}

object SingleDeviceLoginCoroutineScope {

    private val handler = CoroutineExceptionHandler { _, exception ->
    }
    private var scope: CoroutineScope? = null

    fun scopeStatus(): CoroutineScope? {
        return scope
    }

    fun resetScope() {
        scope?.coroutineContext?.cancelChildren()
        scope = null
    }

    fun getScope(): CoroutineScope {
        return scope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
    }
}

object AppAuthStateListener {

    private val _liveAuthStateFromRemote: MutableLiveData<Boolean?> = MutableLiveData()
    val liveAuthStateFromRemote: LiveData<Boolean?> = _liveAuthStateFromRemote

    fun updateLiveAuthStateFromRemote(latestValue: Boolean) {
        _liveAuthStateFromRemote.postValue(latestValue)
    }

    fun resetAll() {
        _liveAuthStateFromRemote.postValue(null)
    }

}

object ServiceError {

    private val _mutableLiveData: MutableLiveData<String> = MutableLiveData()
    val liveData: LiveData<String> = _mutableLiveData

    fun postValue(value: String) {
        _mutableLiveData.postValue(value)
    }

}

fun getTodayDateComponent(milliseconds: Long): Date {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = milliseconds
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

class NetworkInterceptor(context: Context) : Interceptor {

    private val mContext: Context

    init {
        mContext = context
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected) {
            throw NoInternetException()
        }
        val originalRequest = chain.request()
        try {
            return chain.proceed(originalRequest)
        } catch (exception: IOException) {
            if (exception is UnknownHostException) {
                throw NoInternetException()
            } else {
                throw exception
            }
        }
    }

    private val isConnected: Boolean
        get() {
            val connectivityManager =
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isAvailable && netInfo.isConnected
        }
}

class NoInternetException : IOException() {

    override val message: String
        get() = "No Internet Connection."

}

class InstructorLiveListener() {

    private val _mutableLiveData: MutableLiveData<DataStatus<RemoteInstructorProfile>> =
        MutableLiveData()
    val liveData: LiveData<DataStatus<RemoteInstructorProfile>> = _mutableLiveData

    fun setInstructorStatus(status: DataStatus<RemoteInstructorProfile>) {
        _mutableLiveData.postValue(status)
    }

}

class MentionedLinksLiveListener() {

    private val _mutableLiveData: MutableLiveData<DataStatus<List<RemoteMentionedLink>>> =
        MutableLiveData()
    val liveData: LiveData<DataStatus<List<RemoteMentionedLink>>> = _mutableLiveData

    fun setMentionedLinksStatus(status: DataStatus<List<RemoteMentionedLink>>) {
        _mutableLiveData.postValue(status)
    }

}

class ContentDetailsLiveListener() {

    private val _mutableLiveData: MutableLiveData<DataStatus<ReelDetails>> = MutableLiveData()
    val liveData: LiveData<DataStatus<ReelDetails>> = _mutableLiveData

    fun setContentDetailsStatus(status: DataStatus<ReelDetails>) {
        _mutableLiveData.postValue(status)
    }

}

class TokenInterceptor(
    private val authenticationDao: AuthenticationDao,
    private val externalScope: CoroutineScope
) : Interceptor,
    IDTokenListener {

    private var token: String? = null
    private var isComplete: Boolean = false

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val newAccessToken = getIdToken()
            val newRequest = chain.request().newBuilder()
                .header("authorization", "Bearer $newAccessToken")
                .build()
            val response = chain.proceed(newRequest)
            if (response.code == 401) {
                return if (FirebaseAuth.getInstance().currentUser != null) {
                    response.close()
                    val secondToken = getIdToken()
                    val secondRequest = newRequest.newBuilder()
                        .header("authorization", "Bearer $secondToken")
                        .build()
                    chain.proceed(secondRequest)
                } else {
                    response
                }
            }
            return response
        } else {
            val originalRequest = chain.request()
            val response = chain.proceed(originalRequest)
            if (response.code == 401) {
                return if (FirebaseAuth.getInstance().currentUser != null) {
                    response.close()
                    val newAccessToken = getIdToken()
                    val newRequest = originalRequest.newBuilder()
                        .header("authorization", "Bearer $newAccessToken")
                        .build()
                    chain.proceed(newRequest)
                } else {
                    response
                }
            }
            return response
        }
    }

    private fun generateIDToken(idTokenListener: IDTokenListener) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            currentUser.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken: String? = task.result.token
                        idTokenListener.onIDTokenGenerated(idToken)
                    } else {
                        idTokenListener.onIDTokenGenerated()
                    }
                }.addOnFailureListener { idTokenListener.onIDTokenGenerated() }
        }
    }

    private fun getIdToken(): String? {
        val result = authenticationDao.getIDToken()
        return if (result == null) {
            generateIDToken(this)
            while (!isComplete) {
            }
            this.token
        } else {
            this.token = result
            this.token
        }
    }

    override fun onIDTokenGenerated(token: String?) {
        this.token = token
        this.isComplete = true
        externalScope.launch {
            if (token != null) authenticationDao.insertIDToken(IDToken(token = token))
        }
    }

}

interface IDTokenListener {

    fun onIDTokenGenerated(token: String? = null)
}

fun isLinkedInAddress(input: String): Boolean {
    val linkedInPattern = Pattern.compile("^https?://(?:www\\.)?linkedin\\.com/in/[a-zA-Z0-9-]+/?$")
    val matcher = linkedInPattern.matcher(input)
    return matcher.matches()
}

fun extractUsernameFromLinkedInUrl(linkedInUrl: String): String? {
    val trimmedUrl = linkedInUrl.trimEnd('/')
    val parts = trimmedUrl.split("/")
    val linkedInPattern = Regex("^https?://(?:www\\.)?linkedin\\.com/in/([a-zA-Z0-9-]+)/?$")

    if (parts.size >= 2 && linkedInPattern.matches(trimmedUrl)) {
        val usernameIndex = parts.indexOf("in") + 1
        if (usernameIndex < parts.size) {
            return parts[usernameIndex]
        }
    }
    return null
}

fun isGitHubAddress(input: String): Boolean {
    val gitHubPattern = Pattern.compile("^https?://(?:www\\.)?github\\.com/[a-zA-Z0-9_-]+/?$")
    val matcher = gitHubPattern.matcher(input)
    return matcher.matches()
}

fun extractUsernameFromGitHubUrl(gitHubUrl: String): String? {
    //for github url check
    val trimmedUrl = gitHubUrl.trimEnd('/')
    val parts = trimmedUrl.split("/")
    val gitHubPattern = Regex("^https?://(?:www\\.)?github\\.com/([a-zA-Z0-9_-]+)/?$")

    if (parts.size >= 2 && gitHubPattern.matches(trimmedUrl)) {
        val usernameIndex = parts.indexOf("github.com") + 1
        if (usernameIndex < parts.size) {
            return parts[usernameIndex]
        }
    }
    return null
}

fun sanitizeSearchQuery(query: String): String {
    val queryWithEscapedQuotes = query.replace("-", " ").replace("\"", " ").trim()
    return "*$queryWithEscapedQuotes*"
}

class CustomLinearLayoutManager : LinearLayoutManager {

    private var isScrollEnabled = true

    constructor(context: Context?) : super(context)
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setScrollEnabled(flag: Boolean) {
        isScrollEnabled = flag
    }

    override fun canScrollHorizontally(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollHorizontally()
    }
}

fun createShareIntent(shareLink: String, message: String): Intent {
    val intent = Intent()
    intent.setAction(Intent.ACTION_SEND)
    intent.setType("text/plain")
    intent.putExtra(Intent.EXTRA_SUBJECT, "boolean bear")
    val shareMessage = message + shareLink + "\n"
    intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
    return intent
}

fun formatRunTime(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60

    return when {
        seconds < 60 -> String.format("%d %s", seconds, if (seconds == 1L) "sec" else "secs")
        hrs > 0 && mins > 0 -> String.format(
            "%d %s %d %s",
            hrs, if (hrs == 1L) "hr" else "hrs",
            mins, if (mins == 1L) "min" else "mins"
        )

        hrs > 0 -> String.format(
            "%d %s",
            hrs, if (hrs == 1L) "hr" else "hrs"
        )

        mins > 0 -> String.format(
            "%d %s",
            mins, if (mins == 1L) "min" else "mins"
        )

        else -> "0 mins"
    }
}

suspend fun getAppCheckToken(foreRefresh: Boolean = false): String =
    suspendCancellableCoroutine { continuation ->
        Firebase.appCheck.getAppCheckToken(foreRefresh)
            .addOnSuccessListener { appCheckToken: AppCheckToken ->
                continuation.resume(appCheckToken.token)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

class AppCheckInterceptor : Interceptor, IDTokenListener {

    private var token: String? = null
    private var isComplete: Boolean = false

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = getIdToken(refresh = false, replayProtection = false)
        val modifiedRequest = originalRequest.newBuilder()
            .header("X-Firebase-AppCheck", accessToken.toString())
            .build()
        val response = chain.proceed(modifiedRequest)
        return when (response.code) {
            414 -> { // requires token
                response.close()
                val newAccessToken = getIdToken(refresh = false, replayProtection = false)
                val newRequest = originalRequest.newBuilder()
                    .header("X-Firebase-AppCheck", newAccessToken.toString())
                    .build()
                chain.proceed(newRequest)
            }

            415 -> { // for expired token
                response.close()
                val newAccessToken = getIdToken(refresh = true, replayProtection = false)
                val newRequest = originalRequest.newBuilder()
                    .header("X-Firebase-AppCheck", newAccessToken.toString())
                    .build()
                chain.proceed(newRequest)
            }

            416, 417 -> { // for replay protection or token is consumed already
                response.close()
                val newAccessToken = getIdToken(refresh = false, replayProtection = true)
                val newRequest = originalRequest.newBuilder()
                    .header("X-Firebase-AppCheck", newAccessToken.toString())
                    .build()
                chain.proceed(newRequest)
            }

            else -> {
                response
            }
        }
    }

    private fun generateIDToken(
        idTokenListener: IDTokenListener,
        refresh: Boolean = false,
        replayProtection: Boolean = false
    ) {
        if (replayProtection) {
            Firebase.appCheck.limitedUseAppCheckToken
                .addOnSuccessListener { appCheckToken: AppCheckToken ->
                    idTokenListener.onIDTokenGenerated(appCheckToken.token)
                }.addOnFailureListener { _ ->
                    idTokenListener.onIDTokenGenerated()
                }.addOnFailureListener { idTokenListener.onIDTokenGenerated() }
        } else {
            Firebase.appCheck.getAppCheckToken(refresh)
                .addOnSuccessListener { appCheckToken: AppCheckToken ->
                    idTokenListener.onIDTokenGenerated(appCheckToken.token)
                }.addOnFailureListener { _ ->
                    idTokenListener.onIDTokenGenerated()
                }.addOnFailureListener { idTokenListener.onIDTokenGenerated() }
        }
    }

    private fun getIdToken(refresh: Boolean = false, replayProtection: Boolean = false): String? {
        generateIDToken(this, refresh, replayProtection)
        while (!isComplete) {
        }
        return this.token
    }

    override fun onIDTokenGenerated(token: String?) {
        this.token = token
        this.isComplete = true
    }

}

fun showUnauthorizedDeviceDialog(context: Context, title: String?) {
    MaterialAlertDialogBuilder(
        context,
        R.style.MaterialAlertDialog_Material3
    )
        .setTitle(title)
        .setMessage(context.getString(app.efficientbytes.booleanbear.R.string.there_was_an_issue_in_authenticating_your_request))
        .setPositiveButton(context.getString(app.efficientbytes.booleanbear.R.string.dismiss)) { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(false)
        .show()
}