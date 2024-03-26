package app.efficientbytes.androidnow.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.androidnow.services.models.IssueCategory
import app.efficientbytes.androidnow.services.models.Profession
import app.efficientbytes.androidnow.utils.ISSUE_CATEGORY_ADAPTER_TABLE
import app.efficientbytes.androidnow.utils.PROFESSION_ADAPTER_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilityDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionAdapterList(profession: List<Profession>)

    @Query("DELETE FROM $PROFESSION_ADAPTER_TABLE ")
    suspend fun deleteProfessionAdapterList()

    @Query("SELECT * FROM $PROFESSION_ADAPTER_TABLE ")
    fun getProfessionAdapterList(): Flow<MutableList<Profession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueCategoryAdapterList(issueCategories: List<IssueCategory>)

    @Query("DELETE FROM $ISSUE_CATEGORY_ADAPTER_TABLE ")
    suspend fun deleteIssueCategoryAdapterList()

    @Query("SELECT * FROM $ISSUE_CATEGORY_ADAPTER_TABLE ")
    fun getIssueCategoryAdapterList(): Flow<MutableList<IssueCategory>>

}