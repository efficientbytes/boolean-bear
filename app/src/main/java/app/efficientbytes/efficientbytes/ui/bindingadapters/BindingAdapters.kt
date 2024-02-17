package app.efficientbytes.efficientbytes.ui.bindingadapters

import androidx.databinding.BindingAdapter
import app.efficientbytes.efficientbytes.R
import coil.load
import com.google.android.material.imageview.ShapeableImageView

@BindingAdapter("load_image_from_url")
fun ShapeableImageView.loadImageFromUrl(imageUrl: String? = null) {
    this.load(imageUrl) {
        placeholder(R.drawable.shimmer_item_courses_banner)
    }
}