package app.efficientbytes.booleanbear.ui.bindingadapters

import androidx.databinding.BindingAdapter
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.utils.getTimeAgo
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.text.DecimalFormat

@BindingAdapter("load_image_from_url")
fun ShapeableImageView.loadImageFromUrl(imageUrl: String? = null) {
    this.load(imageUrl) {
        placeholder(R.drawable.shimmer_item_courses_banner)
    }
}

@BindingAdapter("uploaded_on")
fun MaterialTextView.uploadedOn(uploadedOn: Long) {
    this.text = getTimeAgo(uploadedOn)
}

@BindingAdapter("seconds_to_minutes")
fun MaterialTextView.secondsToMinutes(seconds: Long) {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    this.text = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

@BindingAdapter("language")
fun MaterialTextView.language(language: String? = null) {
    this.text = when (language) {
        "EN" -> "English"
        "HI" -> "Hindi"
        else -> "N/A"
    }
}

@BindingAdapter("runTime")
fun MaterialTextView.runTime(runTime: Long? = null) {
    val message: String = if (runTime != null) {
        val number = (runTime / 60.0)
        val decimalFormat = DecimalFormat("#.##")
        decimalFormat.format(number)
    } else {
        "0"
    }
    this.text = "$message mins"
}