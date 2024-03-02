package app.efficientbytes.androidnow.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.androidnow.utils.DUMMY_TABLE_NAME

/**
 * This class was created at the time of implementing room database.
 * At that time no entity class was there and room needed one entity class to build the project.
 * That is why this class is created. It serves no business purpose.
 * It has no dao implementation.
 */
@Entity(tableName = DUMMY_TABLE_NAME)
data class Dummy(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String? = null
)
