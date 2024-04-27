package app.efficientbytes.booleanbear.services.models

import app.efficientbytes.booleanbear.database.models.ScreenTimingPerDay
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScreenTimingResponse(
    val screenTimingPerDayList: List<ScreenTimingPerDay>? = null,
    val userAccountId: String? = null
)