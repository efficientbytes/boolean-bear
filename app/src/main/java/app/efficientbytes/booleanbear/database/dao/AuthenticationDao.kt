package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.utils.ID_TOKEN_TABLE
import app.efficientbytes.booleanbear.utils.SINGLE_DEVICE_LOGIN_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthenticationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin)

    @Query("DELETE FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    suspend fun delete()

    @Query("SELECT * FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    fun getSingleDeviceLogin(): Flow<SingleDeviceLogin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIDToken(idToken: IDToken)

    @Query("DELETE FROM $ID_TOKEN_TABLE ")
    suspend fun deleteIDTokenTable()

    @Query("SELECT token FROM $ID_TOKEN_TABLE WHERE rowId = 1")
    fun getIDToken(): String?

}