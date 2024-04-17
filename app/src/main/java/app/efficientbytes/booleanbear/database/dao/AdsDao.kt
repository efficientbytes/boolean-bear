package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.LocalHomePageBanner
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.utils.HOME_PAGE_BANNER_AD_TABLE

@Dao
interface AdsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomePageBannerAds(localHomePageBanner: List<LocalHomePageBanner>)

    @Query("DELETE FROM $HOME_PAGE_BANNER_AD_TABLE ")
    suspend fun deleteHomePageBannerAds()

    @Query("SELECT bannerId,title,imageLink,clickAble,redirectLink,createdOn,startingDate,closingDate FROM $HOME_PAGE_BANNER_AD_TABLE")
    fun getHomePageBannerAds(): List<RemoteHomePageBanner>?

}