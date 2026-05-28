package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyPlanDao {
	@Query("SELECT * FROM weeklyplanentry ORDER BY dayOfWeek")
	fun loadAll(): Flow<List<WeeklyPlanEntry>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun setDay(entry: WeeklyPlanEntry)

	@Query("DELETE FROM weeklyplanentry WHERE dayOfWeek = :day")
	fun clearDay(day: Int): Int
}
