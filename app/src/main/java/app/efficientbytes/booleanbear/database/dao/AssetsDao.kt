package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalShuffledContent
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.RemoteShuffledContent
import app.efficientbytes.booleanbear.utils.INSTRUCTOR_PROFILE_TABLE
import app.efficientbytes.booleanbear.utils.MENTIONED_LINKS_TABLE
import app.efficientbytes.booleanbear.utils.SHUFFLED_CATEGORY_TABLE
import app.efficientbytes.booleanbear.utils.SHUFFLED_CONTENT_TABLE
import app.efficientbytes.booleanbear.utils.SHUFFLED_CONTENT_TABLE_FTS
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShuffledCategories(contentCategories: List<ShuffledCategory>)

    @Query("DELETE FROM $SHUFFLED_CATEGORY_TABLE ")
    suspend fun deleteShuffledCategories()

    @Query("SELECT * FROM $SHUFFLED_CATEGORY_TABLE ORDER BY `index`")
    fun getShuffledCategories(): Flow<MutableList<ShuffledCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShuffledCategoryContents(contents: List<LocalShuffledContent>)

    @Query("SELECT * FROM $SHUFFLED_CONTENT_TABLE WHERE categoryId = :category")
    suspend fun getAllShuffledYoutubeViewContents(category: String): List<RemoteShuffledContent>?

    @Query(
        "SELECT * FROM $SHUFFLED_CONTENT_TABLE" +
                " JOIN $SHUFFLED_CONTENT_TABLE_FTS ON $SHUFFLED_CONTENT_TABLE_FTS.contentId == $SHUFFLED_CONTENT_TABLE.contentId" +
                "  WHERE $SHUFFLED_CONTENT_TABLE_FTS.categoryId = :category" +
                " AND $SHUFFLED_CONTENT_TABLE_FTS.title MATCH :query"
    )
    suspend fun getShuffledContentsByTitle(
        category: String,
        query: String
    ): List<RemoteShuffledContent>?

    @Query(
        "SELECT * FROM $SHUFFLED_CONTENT_TABLE" +
                " JOIN $SHUFFLED_CONTENT_TABLE_FTS ON $SHUFFLED_CONTENT_TABLE_FTS.contentId == $SHUFFLED_CONTENT_TABLE.contentId" +
                "  WHERE $SHUFFLED_CONTENT_TABLE_FTS.categoryId = :category" +
                " AND $SHUFFLED_CONTENT_TABLE_FTS.hashTags MATCH :query"
    )
    suspend fun getShuffledContentsByHashTags(
        category: String,
        query: String
    ): List<RemoteShuffledContent>?

    @Query("SELECT * FROM $SHUFFLED_CONTENT_TABLE WHERE contentId = :content")
    suspend fun getShuffledYoutubeViewContent(content: String): RemoteShuffledContent?

    @Query("DELETE FROM $SHUFFLED_CONTENT_TABLE ")
    suspend fun deleteShuffledYoutubeContentView()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstructorProfile(localInstructorProfile: LocalInstructorProfile)

    @Query("SELECT instructorId,firstName,lastName,bio,oneLineDescription,profession,workingAt,profileImage,coverImage,gitHubUsername,linkedInUsername,skills FROM $INSTRUCTOR_PROFILE_TABLE WHERE instructorId = :id")
    suspend fun getInstructorProfile(id: String): RemoteInstructorProfile?

    @Query("DELETE FROM $INSTRUCTOR_PROFILE_TABLE ")
    suspend fun deleteAllInstructorProfile()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMentionedLink(localMentionedLink: LocalMentionedLink)

    @Query("SELECT linkId,link,createdOn,name FROM $MENTIONED_LINKS_TABLE WHERE linkId = :id")
    suspend fun getMentionedLink(id: String): RemoteMentionedLink?

    @Query("DELETE FROM $MENTIONED_LINKS_TABLE ")
    suspend fun deleteAllMentionedLinks()

}