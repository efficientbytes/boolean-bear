package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.models.UserProfile
import app.efficientbytes.booleanbear.utils.USER_PROFILE_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile): Long

    @Query("DELETE FROM $USER_PROFILE_TABLE")
    suspend fun delete()

    @Query("SELECT * FROM $USER_PROFILE_TABLE ")
    fun getUserProfile(): Flow<UserProfile>

}