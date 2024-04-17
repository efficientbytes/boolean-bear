package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.HOME_PAGE_BANNER_AD_TABLE

@Entity(tableName = HOME_PAGE_BANNER_AD_TABLE)
data class LocalHomePageBanner(
    @PrimaryKey(autoGenerate = false)
    val bannerId: String,
    val title: String? = null,
    val imageLink: String,
    val clickAble: Boolean = false,
    val redirectLink: String? = null,
    val createdOn: Long,
    val startingDate: Long,
    val closingDate: Long
)
