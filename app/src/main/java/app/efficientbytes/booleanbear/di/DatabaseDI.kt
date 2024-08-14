package app.efficientbytes.booleanbear.di

import android.content.Context
import androidx.room.Room
import app.efficientbytes.booleanbear.database.room.AppDatabase
import app.efficientbytes.booleanbear.utils.Pi.DATABASE_NAME

const val appDatabaseName = DATABASE_NAME

fun provideRoomDatabase(context: Context) =
    Room.databaseBuilder(context, AppDatabase::class.java, appDatabaseName).allowMainThreadQueries()
        .fallbackToDestructiveMigration().build()

fun provideUserProfileDao(database: AppDatabase) = database.userProfileDa()

fun provideAuthenticationDao(database: AppDatabase) = database.authenticationDao()

fun provideUtilityDataDao(database: AppDatabase) = database.utilityDao()

fun provideAssetsDao(database: AppDatabase) = database.assetsDao()

fun provideStatisticsDao(database: AppDatabase) = database.statisticsDao()

fun provideAdsDao(database: AppDatabase) = database.adsDao()