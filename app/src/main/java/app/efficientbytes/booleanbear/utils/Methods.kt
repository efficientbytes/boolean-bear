package app.efficientbytes.booleanbear.utils

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.PlayDetails
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.UnknownHostException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

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
    phoneNumberTextInputLayout.error = null
    if (!input.matches(Regex("^[1-9]\\d{9}$"))) {
        phoneNumberTextInputLayout.error = "Invalid phone number format."
        return false
    }
    phoneNumberTextInputLayout.error = null
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

    private val _userProfileLiveListener: MutableLiveData<DataStatus<DocumentSnapshot?>> =
        MutableLiveData()
    val userProfileLiveListener: LiveData<DataStatus<DocumentSnapshot?>> = _userProfileLiveListener
    private val _userProfile: MutableLiveData<DataStatus<UserProfile?>> = MutableLiveData()
    val userProfile: LiveData<DataStatus<UserProfile?>> = _userProfile

    fun postLatestValue(value: DataStatus<DocumentSnapshot?>) {
        _userProfileLiveListener.postValue(value)
    }

    fun postValue(value: DataStatus<UserProfile?>) {
        _userProfile.postValue(value)
    }

}

object SingleDeviceLoginListener {

    private val _mutableLiveData: MutableLiveData<DataStatus<DocumentSnapshot?>> = MutableLiveData()
    val liveData: LiveData<DataStatus<DocumentSnapshot?>> = _mutableLiveData

    fun postValue(value: DataStatus<DocumentSnapshot?>) {
        _mutableLiveData.postValue(value)
    }

}

object AuthStateCoroutineScope {

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.i(AUTH_CUSTOM_COROUTINE_SCOPE, exception.message.toString())
    }
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)

    fun getScope() = scope
}

object CustomAuthStateListener {

    private val _mutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val liveData: LiveData<Boolean> = _mutableLiveData

    fun postValue(value: Boolean) {
        _mutableLiveData.postValue(value)
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

class CustomInterceptor(context: Context) : Interceptor {

    private val mContext: Context

    init {
        mContext = context
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected) {
            throw NoInternetException()
        }
        try {
            val builder: Request.Builder = chain.request().newBuilder()
            return chain.proceed(builder.build())
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

    private val _mutableLiveData: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val liveData: LiveData<DataStatus<Boolean>> = _mutableLiveData

    fun setMentionedLinksStatus(status: DataStatus<Boolean>) {
        _mutableLiveData.postValue(status)
    }

}

class ContentDetailsLiveListener() {

    private val _mutableLiveData: MutableLiveData<DataStatus<PlayDetails?>> = MutableLiveData()
    val liveData: LiveData<DataStatus<PlayDetails?>> = _mutableLiveData

    fun setContentDetailsStatus(status: DataStatus<PlayDetails?>) {
        _mutableLiveData.postValue(status)
    }

}