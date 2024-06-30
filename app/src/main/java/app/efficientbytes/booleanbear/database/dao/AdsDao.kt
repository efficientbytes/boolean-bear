package app.efficientbytes.booleanbear.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.ActiveAdTemplate
import app.efficientbytes.booleanbear.database.models.LocalHomePageBanner
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.utils.ACTIVE_AD_TEMPLATE
import app.efficientbytes.booleanbear.utils.HOME_PAGE_BANNER_AD_TABLE

@Dao
interface AdsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomePageBannerAds(localHomePageBanner: List<LocalHomePageBanner>)

    @Query("DELETE FROM $HOME_PAGE_BANNER_AD_TABLE ")
    suspend fun deleteHomePageBannerAds()

    @Query("SELECT bannerId,title,imageLink,clickAble,redirectLink,createdOn,startingDate,closingDate FROM $HOME_PAGE_BANNER_AD_TABLE")
    fun getHomePageBannerAds(): List<RemoteHomePageBanner>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveAdTemplate(activeAdTemplate: ActiveAdTemplate)

    @Query("DELETE FROM $ACTIVE_AD_TEMPLATE")
    suspend fun deleteActiveAdTemplate()

    @Query("SELECT * FROM $ACTIVE_AD_TEMPLATE")
    fun getActiveAdTemplate(): LiveData<ActiveAdTemplate?>

    @Query("SELECT * FROM $ACTIVE_AD_TEMPLATE")
    suspend fun isAdTemplateActive(): ActiveAdTemplate?

}