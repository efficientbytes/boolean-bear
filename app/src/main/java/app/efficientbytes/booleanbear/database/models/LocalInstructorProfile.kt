package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.Pi.INSTRUCTOR_PROFILE_TABLE

@Entity(INSTRUCTOR_PROFILE_TABLE)
class LocalInstructorProfile(
    @PrimaryKey(autoGenerate = false)
    val instructorId: String,
    val firstName: String,
    val lastName: String? = null,
    val bio: String? = null,
    val oneLineDescription: String? = null,
    val profession: String? = null,
    val workingAt: String? = null,
    val profileImage: String? = null,
    val coverImage: String? = null,
    val gitHubUsername: String? = null,
    val linkedInUsername: String? = null,
    val skills: List<String>? = null
) {
}