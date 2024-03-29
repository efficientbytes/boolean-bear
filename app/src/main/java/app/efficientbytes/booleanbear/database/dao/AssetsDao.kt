package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.ContentCategory
import app.efficientbytes.booleanbear.utils.CONTENT_CATEGORY_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentCategories(contentCategories: List<ContentCategory>)

    @Query("DELETE FROM $CONTENT_CATEGORY_TABLE ")
    suspend fun deleteContentCategories()

    @Query("SELECT * FROM $CONTENT_CATEGORY_TABLE ORDER BY `index`")
    fun getContentCategories(): Flow<MutableList<ContentCategory>>

}