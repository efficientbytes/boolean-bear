package app.efficientbytes.androidnow.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import app.efficientbytes.androidnow.database.dao.AuthenticationDao
import app.efficientbytes.androidnow.database.dao.UserProfileDao
import app.efficientbytes.androidnow.database.models.Dummy
import app.efficientbytes.androidnow.models.SingleDeviceLogin
import app.efficientbytes.androidnow.models.UserProfile

@Database(
    entities = [Dummy::class, UserProfile::class, SingleDeviceLogin::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDa(): UserProfileDao
    abstract fun authenticationDao(): AuthenticationDao
}