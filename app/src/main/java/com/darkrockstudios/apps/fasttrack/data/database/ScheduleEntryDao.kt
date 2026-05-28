package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEntryDao {
	@Query("SELECT * FROM scheduleentry ORDER BY uid DESC")
	fun loadAll(): Flow<List<ScheduleEntry>>

	@Insert
	fun insert(entry: ScheduleEntry): Long

	@Update
	fun update(entry: ScheduleEntry): Int

	@Delete
	fun delete(entry: ScheduleEntry): Int

	@Query("DELETE FROM scheduleentry WHERE uid = :uid")
	fun deleteByUid(uid: Int): Int

	@Query("UPDATE scheduleentry SET isActive = 0")
	fun deactivateAll(): Int

	@Query("UPDATE scheduleentry SET isActive = 1 WHERE uid = :uid")
	fun activate(uid: Int): Int
}
