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
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.models.CategoryType
import app.efficientbytes.booleanbear.repositories.AssetsRepository
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.YoutubeContentView

class HomeViewModel(
    private val assetsRepository: AssetsRepository,
) : ViewModel(),
    LifecycleEventObserver, AssetsRepository.CategoryListener, AssetsRepository.ContentListener {

    val contentCategoriesFromDB: LiveData<MutableList<ShuffledCategory>> =
        assetsRepository.categoriesFromDB.asLiveData()
    private val _categories: MutableLiveData<DataStatus<Boolean>> = MutableLiveData()
    val categories: LiveData<DataStatus<Boolean>> = _categories

    fun getShuffledCategories() {
        assetsRepository.downloadShuffledCategories(this@HomeViewModel)
    }

    private val _youtubeContentViewList: MutableLiveData<DataStatus<List<YoutubeContentView>>> =
        MutableLiveData()
    val youtubeContentViewList: LiveData<DataStatus<List<YoutubeContentView>>> =
        _youtubeContentViewList

    fun getYoutubeViewContentsUnderShuffledCategory(categoryId: String) {
        assetsRepository.getAllContent(categoryId, CategoryType.SHUFFLED, this@HomeViewModel)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> {
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
        assetsRepository.getAllContent(categoryId, CategoryType.SHUFFLED, this@HomeViewModel)
    }

    override fun onContentsDataStatusChanged(status: DataStatus<List<YoutubeContentView>>) {
        _youtubeContentViewList.postValue(status)
    }

}