package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteHomePageBanner(
    val bannerId: String,
    val title: String? = null,
    val imageLink: String,
    val clickAble: Boolean = false,
    val redirectLink: String? = null,
    val createdOn: Long,
    val startingDate: Long,
    val closingDate: Long
)

@JsonClass(generateAdapter = true)
data class HomePageBannerListResponse(
    val data: List<RemoteHomePageBanner>? = null,
    val message: String? = null
)