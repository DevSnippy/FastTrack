package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
	@Query("SELECT * FROM foodentry ORDER BY timestamp DESC")
	fun loadAll(): Flow<List<FoodEntry>>

	@Insert
	fun insert(entry: FoodEntry): Long

	@Update
	fun update(entry: FoodEntry): Int

	@Delete
	fun delete(entry: FoodEntry): Int

	@Query("DELETE FROM foodentry WHERE uid = :uid")
	fun deleteByUid(uid: Int): Int

	@Query("UPDATE foodentry SET calories = :calories WHERE uid = :uid")
	fun updateCalories(uid: Int, calories: Int): Int
}
