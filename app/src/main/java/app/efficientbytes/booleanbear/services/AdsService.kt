package app.efficientbytes.booleanbear.services

import app.efficientbytes.booleanbear.services.models.HomePageBannerListResponse
import retrofit2.Response
import retrofit2.http.GET

interface AdsService {

    @GET("ads/banners/home-page")
    suspend fun getAdsForHomePageBanner(): Response<HomePageBannerListResponse>

}