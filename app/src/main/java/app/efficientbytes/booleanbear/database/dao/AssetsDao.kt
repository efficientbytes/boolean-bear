package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalReel
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.RemoteShuffledContent
import app.efficientbytes.booleanbear.utils.INSTRUCTOR_PROFILE_TABLE
import app.efficientbytes.booleanbear.utils.MENTIONED_LINKS_TABLE
import app.efficientbytes.booleanbear.utils.REELS_TABLE
import app.efficientbytes.booleanbear.utils.REELS_TABLE_FTS
import app.efficientbytes.booleanbear.utils.SHUFFLED_CATEGORY_TABLE
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
    suspend fun insertReels(contents: List<LocalReel>)

    @Query("SELECT * FROM $REELS_TABLE WHERE topicId = :topic")
    suspend fun getReels(topic: String): List<RemoteShuffledContent>?

    @Query(
        "SELECT * FROM $REELS_TABLE" +
                " JOIN $REELS_TABLE_FTS ON $REELS_TABLE_FTS.reelId == $REELS_TABLE.reelId" +
                "  WHERE $REELS_TABLE_FTS.topicId = :topic" +
                " AND $REELS_TABLE_FTS.title MATCH :query"
    )
    suspend fun getReelsByTitle(
        topic: String,
        query: String
    ): List<RemoteShuffledContent>?

    @Query(
        "SELECT * FROM $REELS_TABLE" +
                " JOIN $REELS_TABLE_FTS ON $REELS_TABLE_FTS.reelId == $REELS_TABLE.reelId" +
                "  WHERE $REELS_TABLE_FTS.topicId = :category" +
                " AND $REELS_TABLE_FTS.hashTags MATCH :query"
    )
    suspend fun getReelsByHashTags(
        category: String,
        query: String
    ): List<RemoteShuffledContent>?

    @Query("SELECT * FROM $REELS_TABLE WHERE reelId = :reel")
    suspend fun getReel(reel: String): RemoteShuffledContent?

    @Query("DELETE FROM $REELS_TABLE ")
    suspend fun deleteAllReels()

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