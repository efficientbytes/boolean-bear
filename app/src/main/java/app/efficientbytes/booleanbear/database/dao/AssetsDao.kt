package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.LocalCourse
import app.efficientbytes.booleanbear.database.models.LocalCourseTopic
import app.efficientbytes.booleanbear.database.models.LocalCourseWaitingList
import app.efficientbytes.booleanbear.database.models.LocalInstructorProfile
import app.efficientbytes.booleanbear.database.models.LocalMentionedLink
import app.efficientbytes.booleanbear.database.models.LocalReel
import app.efficientbytes.booleanbear.database.models.LocalReelTopic
import app.efficientbytes.booleanbear.services.models.RemoteCourse
import app.efficientbytes.booleanbear.services.models.RemoteCourseTopic
import app.efficientbytes.booleanbear.services.models.RemoteInstructorProfile
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic
import app.efficientbytes.booleanbear.utils.COURSE_TABLE
import app.efficientbytes.booleanbear.utils.COURSE_TOPIC_TABLE
import app.efficientbytes.booleanbear.utils.INSTRUCTOR_PROFILE_TABLE
import app.efficientbytes.booleanbear.utils.MENTIONED_LINKS_TABLE
import app.efficientbytes.booleanbear.utils.REELS_TABLE
import app.efficientbytes.booleanbear.utils.REELS_TABLE_FTS
import app.efficientbytes.booleanbear.utils.REEL_TOPICS_TABLE

@Dao
interface AssetsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReelTopics(reelTopics: List<LocalReelTopic>)

    @Query("DELETE FROM $REEL_TOPICS_TABLE ")
    suspend fun deleteReelTopics()

    @Query("SELECT * FROM $REEL_TOPICS_TABLE ORDER BY displayIndex")
    fun getReelTopics(): List<RemoteReelTopic>?

    @Query("SELECT * FROM $REEL_TOPICS_TABLE WHERE topicId = :topic")
    fun getReelTopicDetails(topic: String): RemoteReelTopic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(contents: List<LocalReel>)

    @Query("SELECT * FROM $REELS_TABLE WHERE topicId = :topic")
    suspend fun getReels(topic: String): List<RemoteReel>?

    @Query(
        "SELECT * FROM $REELS_TABLE" +
                " JOIN $REELS_TABLE_FTS ON $REELS_TABLE_FTS.reelId == $REELS_TABLE.reelId" +
                "  WHERE $REELS_TABLE_FTS.topicId = :topic" +
                " AND $REELS_TABLE_FTS.title MATCH :query"
    )
    suspend fun getReelsByTitle(
        topic: String,
        query: String
    ): List<RemoteReel>?

    @Query(
        "SELECT * FROM $REELS_TABLE" +
                " JOIN $REELS_TABLE_FTS ON $REELS_TABLE_FTS.reelId == $REELS_TABLE.reelId" +
                "  WHERE $REELS_TABLE_FTS.topicId = :topic" +
                " AND $REELS_TABLE_FTS.hashTags MATCH :query"
    )
    suspend fun getReelsByHashTags(
        topic: String,
        query: String
    ): List<RemoteReel>?

    @Query("SELECT * FROM $REELS_TABLE WHERE reelId = :reel")
    suspend fun getReel(reel: String): RemoteReel?

    @Query("DELETE FROM $REELS_TABLE ")
    suspend fun deleteReels()

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseTopic(courseTopic: LocalCourseTopic)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseTopic(courseTopic: List<LocalCourseTopic>)

    @Query("DELETE FROM $COURSE_TOPIC_TABLE ")
    suspend fun deleteCourseTopics()

    @Query("SELECT * FROM $COURSE_TOPIC_TABLE ORDER BY displayIndex")
    fun getCourseTopics(): List<RemoteCourseTopic>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<LocalCourse>)

    @Query("SELECT * FROM $COURSE_TABLE WHERE topicId = :topic")
    suspend fun getCourses(topic: String): List<RemoteCourse>?

    @Query("DELETE FROM $COURSE_TABLE")
    suspend fun deleteCourses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseWaitingList(waitingList: LocalCourseWaitingList)

    @Query("SELECT COUNT(*) > 0 FROM course_waiting_list WHERE courseId = :course")
    fun userHasJoinedWaitingList(course: String): Boolean

}