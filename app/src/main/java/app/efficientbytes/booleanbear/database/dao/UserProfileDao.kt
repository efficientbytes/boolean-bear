package app.efficientbytes.booleanbear.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.LocalNotificationToken
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.utils.Pi.FCM_TOKEN_TABLE
import app.efficientbytes.booleanbear.utils.Pi.USER_PROFILE_TABLE

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile): Long

    @Query("DELETE FROM $USER_PROFILE_TABLE")
    suspend fun deleteUserProfile()

    @Query("SELECT * FROM $USER_PROFILE_TABLE ")
    fun getUserProfile(): LiveData<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFCMToken(localNotificationToken: LocalNotificationToken)

    @Query("DELETE FROM $FCM_TOKEN_TABLE")
    suspend fun deleteFCMToken()

    @Query("SELECT token FROM $FCM_TOKEN_TABLE ")
    fun getFCMToken(): String?

}