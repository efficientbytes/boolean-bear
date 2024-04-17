package app.efficientbytes.booleanbear.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.efficientbytes.booleanbear.database.dao.AdsDao
import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.dao.AuthenticationDao
import app.efficientbytes.booleanbear.database.dao.StatisticsDao
import app.efficientbytes.booleanbear.database.dao.UserProfileDao
import app.efficientbytes.booleanbear.database.dao.UtilityDataDao
import app.efficientbytes.booleanbear.database.models.CategoryContentId
import app.efficientbytes.booleanbear.database.models.Dummy
import app.efficientbytes.booleanbear.database.models.ListConverter
import app.efficientbytes.booleanbear.database.models.LocalHomePageBanner
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalYoutubeContentView
import app.efficientbytes.booleanbear.database.models.ScreenTiming
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.services.models.IssueCategory
import app.efficientbytes.booleanbear.services.models.Profession
import app.efficientbytes.booleanbear.services.models.YoutubeContentView

@Database(
    entities = [Dummy::class, UserProfile::class, SingleDeviceLogin::class, Profession::class, IssueCategory::class, ShuffledCategory::class, CategoryContentId::class, ScreenTiming::class, YoutubeContentView::class, LocalYoutubeContentView::class, LocalInstructorProfile::class, LocalMentionedLink::class, LocalHomePageBanner::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDa(): UserProfileDao
    abstract fun authenticationDao(): AuthenticationDao
    abstract fun utilityDao(): UtilityDataDao
    abstract fun assetsDao(): AssetsDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun adsDao(): AdsDao
}