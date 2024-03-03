package app.efficientbytes.androidnow.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import app.efficientbytes.androidnow.database.dao.UserProfileDao
import app.efficientbytes.androidnow.database.models.Dummy
import app.efficientbytes.androidnow.models.UserProfile

@Database(entities = [Dummy::class, UserProfile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDa(): UserProfileDao
}