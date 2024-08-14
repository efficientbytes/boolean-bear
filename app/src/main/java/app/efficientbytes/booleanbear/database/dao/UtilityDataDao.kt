package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.models.IssueCategory
import app.efficientbytes.booleanbear.models.Profession
import app.efficientbytes.booleanbear.utils.Pi.ISSUE_CATEGORY_ADAPTER_TABLE
import app.efficientbytes.booleanbear.utils.Pi.PROFESSION_ADAPTER_TABLE

@Dao
interface UtilityDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionAdapterList(profession: List<Profession>)

    @Query("DELETE FROM $PROFESSION_ADAPTER_TABLE ")
    suspend fun deleteProfessionAdapterList()

    @Query("SELECT * FROM $PROFESSION_ADAPTER_TABLE ORDER BY `index` ASC ")
    suspend fun getProfessionAdapterList(): List<Profession>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueCategoryAdapterList(issueCategories: List<IssueCategory>)

    @Query("DELETE FROM $ISSUE_CATEGORY_ADAPTER_TABLE ")
    suspend fun deleteIssueCategoryAdapterList()

    @Query("SELECT * FROM $ISSUE_CATEGORY_ADAPTER_TABLE ORDER BY `index` ASC ")
    suspend fun getIssueCategoryAdapterList(): List<IssueCategory>?

}