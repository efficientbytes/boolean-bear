package app.efficientbytes.booleanbear.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.efficientbytes.booleanbear.utils.Pi.ACTIVE_AD_TEMPLATE

@Entity(tableName = ACTIVE_AD_TEMPLATE)
data class ActiveAdTemplate(
    @PrimaryKey(autoGenerate = false)
    val templateId: String,
    val isActive: Boolean,
    val enabledAt: Long
)
