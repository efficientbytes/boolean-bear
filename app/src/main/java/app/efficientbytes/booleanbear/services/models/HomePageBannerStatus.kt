package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomePageBannerStatus(
    val banners: List<RemoteHomePageBanner>? = null,
    val message: String? = null
)
