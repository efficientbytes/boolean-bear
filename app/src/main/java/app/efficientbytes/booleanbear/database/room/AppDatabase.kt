package app.efficientbytes.booleanbear.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import app.efficientbytes.booleanbear.database.dao.AssetsDao
import app.efficientbytes.booleanbear.database.dao.AuthenticationDao
import app.efficientbytes.booleanbear.database.dao.UserProfileDao
import app.efficientbytes.booleanbear.database.dao.UtilityDataDao
import app.efficientbytes.booleanbear.database.models.CategoryContentId
import app.efficientbytes.booleanbear.database.models.ContentCategory
import app.efficientbytes.booleanbear.database.models.Dummy
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.services.models.IssueCategory
import app.efficientbytes.booleanbear.services.models.Profession

@Database(
    entities = [Dummy::class, UserProfile::class, SingleDeviceLogin::class, Profession::class, IssueCategory::class, ContentCategory::class, CategoryContentId::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDa(): UserProfileDao
    abstract fun authenticationDao(): AuthenticationDao
    abstract fun utilityDao(): UtilityDataDao
    abstract fun assetsDao(): AssetsDao
}