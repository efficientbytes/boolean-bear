package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.MENTIONED_LINKS_TABLE

@Entity(tableName = MENTIONED_LINKS_TABLE)
data class LocalMentionedLink(
    val createdOn: Long? = -1L,
    val link: String? = null,
    @PrimaryKey(autoGenerate = false)
    val linkId: String,
    val name: String? = null
)
