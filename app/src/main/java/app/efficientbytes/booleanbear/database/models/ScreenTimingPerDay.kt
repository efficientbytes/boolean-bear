package app.efficientbytes.booleanbear.database.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScreenTimingPerDay(
    val date: Long,
    val screenTime: Double
)
