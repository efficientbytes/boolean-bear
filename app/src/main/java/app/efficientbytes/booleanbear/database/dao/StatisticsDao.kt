package app.efficientbytes.booleanbear.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.efficientbytes.booleanbear.database.models.ScreenTiming
import app.efficientbytes.booleanbear.database.models.ScreenTimingPerDay

@Dao
interface StatisticsDao {

    @Query("SELECT * FROM user_screen_timing")
    suspend fun screenTimingIsEmpty(): List<ScreenTiming>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun noteDownScreenOpeningTime(screenTiming: ScreenTiming)

    @Query("UPDATE user_screen_timing SET closed = :closeTime WHERE date = :currentDate AND rowId = (SELECT MAX(rowId) FROM user_screen_timing WHERE date = :currentDate)")
    suspend fun noteDownScreenClosingTime(currentDate: Long, closeTime: Long)

    @Query("SELECT date, SUM(closed - opened)/60000 AS screenTime FROM user_screen_timing WHERE closed!=-1 GROUP BY date")
    suspend fun getTotalScreenTimePerDayBasis(): List<ScreenTimingPerDay>

    @Query("SELECT date, SUM(closed - opened)/60000 AS screenTime FROM user_screen_timing WHERE closed!=-1 AND date != :currentDate GROUP BY date")
    suspend fun getTotalScreenTimePerDayBasisForAllDayExceptFor(currentDate: Long): List<ScreenTimingPerDay>

    @Query("DELETE FROM user_screen_timing WHERE date!= :today")
    suspend fun deleteScreenTimingForAllDayExceptFor(today: Long)

    @Query("DELETE FROM user_screen_timing")
    suspend fun deleteScreenTimeForAllDay()

}