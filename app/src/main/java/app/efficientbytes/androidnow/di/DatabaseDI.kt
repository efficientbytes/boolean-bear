package app.efficientbytes.androidnow.di

import android.content.Context
import androidx.room.Room
import app.efficientbytes.androidnow.database.room.AppDatabase
import app.efficientbytes.androidnow.utils.DATABASE_NAME

const val appDatabaseName = DATABASE_NAME

fun provideRoomDatabase(context: Context) =
    Room.databaseBuilder(context, AppDatabase::class.java, appDatabaseName).allowMainThreadQueries()
        .fallbackToDestructiveMigration().build()

fun provideUserProfileDao(database: AppDatabase) = database.userProfileDa()

fun provideAuthenticationDao(database: AppDatabase) = database.authenticationDao()