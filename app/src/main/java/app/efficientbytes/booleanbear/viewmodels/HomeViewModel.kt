package app.efficientbytes.booleanbear.viewmodels

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.models.CategoryType
import app.efficientbytes.booleanbear.repositories.AdsRepository
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.services.models.RemoteShuffledContent
import app.efficientbytes.booleanbear.ui.fragments.HomeFragment
import kotlinx.coroutines.launch

class HomeViewModel(
    private val assetsRepository: AssetsRepository,
    private val adsRepository: AdsRepository
) : ViewModel(),
    LifecycleEventObserver, AssetsRepository.CategoryListener, AssetsRepository.ContentListener,
    AdsRepository.HomePageAdsListener {

    val contentCategoriesFromDB: LiveData<MutableList<ShuffledCategory>> =
        assetsRepository.categoriesFromDB.asLiveData()
    private val _categories: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val categories: LiveData<DataStatus<Boolean>> = _categories

    fun getShuffledCategories() {
        assetsRepository.downloadShuffledCategories(this@HomeViewModel)
    }

    private val _remoteShuffledContentList: MutableLiveData<DataStatus<List<RemoteShuffledContent>>> =
        MutableLiveData()
    val remoteShuffledContentList: LiveData<DataStatus<List<RemoteShuffledContent>>> =
        _remoteShuffledContentList

    fun getYoutubeViewContentsUnderShuffledCategory(categoryId: String) {
        assetsRepository.getAllContent(categoryId, CategoryType.SHUFFLED, this@HomeViewModel)
    }

    private val _viewPagerBannerAds: MutableLiveData<DataStatus<List<RemoteHomePageBanner>>> =
        MutableLiveData()
    val viewPagerBannerAds: LiveData<DataStatus<List<RemoteHomePageBanner>>> =
        _viewPagerBannerAds

    fun getHomePageBannerAds() {
        adsRepository.getHomePageBannerAds(this@HomeViewModel)
    }

    private val _searchResult: MutableLiveData<DataStatus<List<RemoteShuffledContent>>> =
        MutableLiveData()
    val searchResult: LiveData<DataStatus<List<RemoteShuffledContent>>> = _searchResult

    fun getSearchContents(categoryId: String, query: String = "") {
        Log.i("HOME VIEW MODEL", "Search category is $categoryId  and search query is $query")
        viewModelScope.launch {
            when {
                query.isEmpty() -> {
                    val allContents = assetsRepository.getSearchContents(categoryId)
                    if (allContents.isNullOrEmpty()) {
                        _searchResult.postValue(DataStatus.emptyResult())
                    } else {
                        _searchResult.postValue(DataStatus.success(allContents))
                    }
                }

                query.isNotEmpty() && query.startsWith("#") -> {

                }

                query.isNotEmpty() && (!query.startsWith("#")) -> {
                    val allContents = assetsRepository.getSearchContents(categoryId, query)
                    if (allContents.isNullOrEmpty()) {
                        _searchResult.postValue(DataStatus.emptyResult())
                    } else {
                        _searchResult.postValue(DataStatus.success(allContents))
                    }
                }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getHomePageBannerAds()
                getShuffledCategories()
            }

            ON_START -> {

            }

            ON_RESUME -> {

            }

            ON_PAUSE -> {

            }

            ON_STOP -> {

            }

            ON_DESTROY -> {

            }

            ON_ANY -> {

            }
        }
    }

    override fun onCategoryDataStatusChanged(status: DataStatus<Boolean>) {
        _categories.postValue(status)
    }

    override fun onIndex1CategoryFound(categoryId: String) {
        HomeFragment.selectedCategoryId = categoryId
        HomeFragment.selectedCategoryPosition = 1
        assetsRepository.getAllContent(categoryId, CategoryType.SHUFFLED, this@HomeViewModel)
    }

    override fun onContentsDataStatusChanged(status: DataStatus<List<RemoteShuffledContent>>) {
        _remoteShuffledContentList.postValue(status)
    }

    override fun onHomePageAdsStatusChanged(status: DataStatus<List<RemoteHomePageBanner>>) {
        _viewPagerBannerAds.postValue(status)
    }

}