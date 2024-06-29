package app.efficientbytes.booleanbear.repositories

import androidx.lifecycle.LiveData
import app.efficientbytes.booleanbear.database.dao.AdsDao
import app.efficientbytes.booleanbear.database.models.ActiveAdTemplate
import app.efficientbytes.booleanbear.database.models.LocalHomePageBanner
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.AdsService
import app.efficientbytes.booleanbear.services.models.HomePageBannerListResponse
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.utils.NoInternetException
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class AdsRepository(
    private val externalScope: CoroutineScope,
    private val adsService: AdsService,
    private val adsDao: AdsDao
) {

    private val gson = Gson()

    private fun fetchHomePageBannerAds() = flow {
        emit(DataStatus.loading())
        try {
            val response = adsService.getAdsForHomePageBanner()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val body = response.body()
                    if (body != null) {
                        val remoteHomePageBanner = body.data
                        if (remoteHomePageBanner != null) {
                            if (remoteHomePageBanner.isEmpty()) {
                                emit(DataStatus.emptyResult())
                            } else {
                                emit(DataStatus.success(remoteHomePageBanner))
                            }
                        }
                    } else {
                        emit(DataStatus.emptyResult())
                    }
                }

                responseCode >= 400 -> {
                    val errorResponse: HomePageBannerListResponse = gson.fromJson(
                        response.errorBody()!!.string(),
                        HomePageBannerListResponse::class.java
                    )
                    emit(DataStatus.failed(errorResponse.message.toString()))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }

    fun getHomePageBannerAds(homePageAdsListener: HomePageAdsListener) {
        externalScope.launch {
            val result = adsDao.getHomePageBannerAds()
            if (!result.isNullOrEmpty()) {
                homePageAdsListener.onHomePageAdsStatusChanged(DataStatus.success(result))
            } else {
                fetchHomePageBannerAds().collect {
                    when (it.status) {
                        DataStatus.Status.EmptyResult -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.emptyResult()
                        )

                        DataStatus.Status.Failed -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.failed(it.message.toString())
                        )

                        DataStatus.Status.Loading -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.loading()
                        )

                        DataStatus.Status.NoInternet -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.noInternet()
                        )

                        DataStatus.Status.Success -> {
                            val list = it.data
                            if (!list.isNullOrEmpty()) {
                                val localHomePageBannerList = list.map { i ->
                                    LocalHomePageBanner(
                                        i.bannerId,
                                        i.title,
                                        i.imageLink,
                                        i.clickAble,
                                        i.redirectLink,
                                        i.createdOn,
                                        i.startingDate,
                                        i.closingDate
                                    )
                                }
                                homePageAdsListener.onHomePageAdsStatusChanged(
                                    DataStatus.success(
                                        list
                                    )
                                )
                                adsDao.insertHomePageBannerAds(localHomePageBannerList)
                            } else {
                                homePageAdsListener.onHomePageAdsStatusChanged(DataStatus.emptyResult())
                            }
                        }

                        DataStatus.Status.TimeOut -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.timeOut()
                        )

                        DataStatus.Status.UnAuthorized -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.unAuthorized()
                        )

                        DataStatus.Status.UnKnownException -> homePageAdsListener.onHomePageAdsStatusChanged(
                            DataStatus.unknownException(it.message.toString())
                        )
                    }
                }
            }
        }
    }

    val getActiveAdTemplate: LiveData<ActiveAdTemplate?> = adsDao.getActiveAdTemplate()

    suspend fun insertActiveAdTemplate(activeAdTemplate: ActiveAdTemplate) {
        adsDao.deleteActiveAdTemplate()
        adsDao.insertActiveAdTemplate(activeAdTemplate)
    }

    suspend fun deleteActiveAdsTemplate() {
        adsDao.deleteActiveAdTemplate()
    }

    interface HomePageAdsListener {

        fun onHomePageAdsStatusChanged(status: DataStatus<List<RemoteHomePageBanner>>)
    }


}