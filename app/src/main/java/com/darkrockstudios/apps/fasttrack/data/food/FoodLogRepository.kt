package com.darkrockstudios.apps.fasttrack.data.food

import kotlinx.coroutines.flow.Flow

interface FoodLogRepository {
	fun loadAll(): Flow<List<FoodLogEntry>>
	fun addEntry(description: String, timestamp: Long, calories: Int? = null)
	fun updateEntry(id: Int, description: String, timestamp: Long, calories: Int?): Boolean
	fun delete(entry: FoodLogEntry): Boolean
	fun updateCalories(id: Int, calories: Int): Boolean
}
