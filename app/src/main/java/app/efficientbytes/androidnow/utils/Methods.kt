package app.efficientbytes.androidnow.utils

import app.efficientbytes.androidnow.models.SingleDeviceLogin
import com.google.android.material.textfield.TextInputLayout
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
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - timestamp
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