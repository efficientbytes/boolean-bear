package app.efficientbytes.booleanbear.viewmodels

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
import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.ShuffledCategoryContentIds
import app.efficientbytes.booleanbear.services.models.YoutubeContentView
import app.efficientbytes.booleanbear.ui.fragments.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(
    private val assetsRepository: AssetsRepository,
    private val assetsDao: AssetsDao,
    private val externalScope: CoroutineScope,
) : ViewModel(),
    LifecycleEventObserver {

    val contentCategoriesFromDB: LiveData<MutableList<ShuffledCategory>> =
        assetsRepository.categoriesFromDB.asLiveData()

    private fun getCategories() {
        externalScope.launch(Dispatchers.IO) {
            assetsRepository.getShuffledCategories().collect {
                when (it.status) {
                    DataStatus.Status.Failed -> {

                    }

                    DataStatus.Status.Loading -> {

                    }

                    DataStatus.Status.Success -> {
                        it.data?.let { list ->
                            val serviceContentCategory =
                                list.categoryList.find { serviceContentCategory -> serviceContentCategory.index == 1 }
                            serviceContentCategory?.let { category ->
                                HomeFragment.selectedCategoryId = category.id
                                HomeFragment.selectedCategoryPosition = 0
                                getShuffledContentIdsFor(category.id)
                            }
                            assetsRepository.saveShuffledCategories(list.categoryList)
                        }
                    }

                    DataStatus.Status.EmptyResult -> {

                    }
                    DataStatus.Status.NoInternet -> {

                    }
                    DataStatus.Status.TimeOut -> {

                    }
                    DataStatus.Status.UnAuthorized -> {

                    }
                    DataStatus.Status.UnKnownException -> {

                    }
                }
            }
        }
    }

    private val _shuffledCategoryContentIds: MutableLiveData<DataStatus<ShuffledCategoryContentIds?>> =
        MutableLiveData()
    val shuffledCategoryContentIds: LiveData<DataStatus<ShuffledCategoryContentIds?>> = _shuffledCategoryContentIds
    private var contentIdListJob: Job? = null
    private var youtubeContentViewJob: Job? = null

    fun getShuffledContentIdsFor(categoryId: String) {
        if (contentIdListJob != null || youtubeContentViewJob != null) {
            contentIdListJob?.cancel()
            youtubeContentViewJob?.cancel()
            contentIdListJob = null
            youtubeContentViewJob = null
        }
        contentIdListJob = externalScope.launch {
            assetsRepository.getContentIdsUnderShuffledCategoryForCategoryId(categoryId).collect {
                _shuffledCategoryContentIds.postValue(it)
            }
        }
    }

    private val _youtubeContentViewList: MutableLiveData<DataStatus<List<YoutubeContentView>>> =
        MutableLiveData()
    val youtubeContentViewList: LiveData<DataStatus<List<YoutubeContentView>>> =
        _youtubeContentViewList

    fun getYoutubeContentViewForListOf(list: List<String>) {
        youtubeContentViewJob = externalScope.launch(Dispatchers.IO) {
            _youtubeContentViewList.postValue(DataStatus.loading())
            val result = assetsRepository.getYoutubeTypeContentForListOf(list)
            if (result.isEmpty()) {
                _youtubeContentViewList.postValue(DataStatus.failed("No search results"))
            } else {
                _youtubeContentViewList.postValue(DataStatus.success(result))
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
                getCategories()
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

}