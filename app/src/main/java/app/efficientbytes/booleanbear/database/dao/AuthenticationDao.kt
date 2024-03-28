package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.utils.SINGLE_DEVICE_LOGIN_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthenticationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin): Long

    @Query("DELETE FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    suspend fun delete()

    @Query("SELECT * FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    fun getSingleDeviceLogin(): Flow<SingleDeviceLogin>

}