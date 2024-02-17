package app.efficientbytes.efficientbytes.ui.bindingadapters

import androidx.databinding.BindingAdapter
import app.efficientbytes.efficientbytes.R
import app.efficientbytes.efficientbytes.utils.getTimeAgo
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

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