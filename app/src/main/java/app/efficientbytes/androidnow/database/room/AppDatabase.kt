package app.efficientbytes.androidnow.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import app.efficientbytes.androidnow.database.models.Dummy

@Database(entities = [Dummy::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
}