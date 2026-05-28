package com.darkrockstudios.apps.fasttrack.data.food

import com.darkrockstudios.apps.fasttrack.data.database.FoodEntry
import kotlinx.coroutines.flow.Flow

interface FoodLogDatasource {
	fun loadAll(): Flow<List<FoodEntry>>
	fun insert(entry: FoodEntry): Long
	fun deleteByUid(uid: Int): Int
	fun updateCalories(uid: Int, calories: Int): Int
}
