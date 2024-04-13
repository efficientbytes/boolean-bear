package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalYoutubeContentView
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.YoutubeContentView
import app.efficientbytes.booleanbear.utils.INSTRUCTOR_PROFILE_TABLE
import app.efficientbytes.booleanbear.utils.LOCAL_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE
import app.efficientbytes.booleanbear.utils.MENTIONED_LINKS_TABLE
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
    suspend fun insertShuffledCategoryContents(contents: List<LocalYoutubeContentView>)

    @Query("SELECT * FROM $LOCAL_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE WHERE categoryId = :category")
    suspend fun getAllShuffledYoutubeViewContents(category: String): List<YoutubeContentView>

    @Query("SELECT * FROM $LOCAL_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE WHERE contentId = :content")
    suspend fun getShuffledYoutubeViewContent(content: String): YoutubeContentView?

    @Query("DELETE FROM $LOCAL_SHUFFLED_YOUTUBE_CONTENT_VIEW_TABLE ")
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