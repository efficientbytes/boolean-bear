package app.efficientbytes.booleanbear.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.IDToken
import app.efficientbytes.booleanbear.models.LocalBooleanFlag
import app.efficientbytes.booleanbear.models.SingleDeviceLogin
import app.efficientbytes.booleanbear.utils.BOOLEAN_FLAG_TABLE
import app.efficientbytes.booleanbear.utils.ID_TOKEN_TABLE
import app.efficientbytes.booleanbear.utils.SINGLE_DEVICE_LOGIN_TABLE

@Dao
interface AuthenticationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleDeviceLogin(singleDeviceLogin: SingleDeviceLogin)

    @Query("DELETE FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    suspend fun deleteSingleDeviceLogin()

    @Query("SELECT * FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    fun getSingleDeviceLogin(): SingleDeviceLogin?

    @Query("SELECT * FROM $SINGLE_DEVICE_LOGIN_TABLE ")
    fun getLiveSingleDeviceLogin(): LiveData<SingleDeviceLogin?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIDToken(idToken: IDToken)

    @Query("DELETE FROM $ID_TOKEN_TABLE ")
    suspend fun deleteIDTokenTable()

    @Query("SELECT token FROM $ID_TOKEN_TABLE WHERE rowId = 1")
    fun getIDToken(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasswordCreatedFlag(localBooleanFlag: LocalBooleanFlag)

    @Query(" DELETE FROM $BOOLEAN_FLAG_TABLE WHERE flagKey = :name")
    suspend fun deletePasswordCreatedFlag(name: String)

    @Query(" SELECT value FROM $BOOLEAN_FLAG_TABLE WHERE flagKey = :name")
    fun getPasswordCreated(name: String): Boolean?

}