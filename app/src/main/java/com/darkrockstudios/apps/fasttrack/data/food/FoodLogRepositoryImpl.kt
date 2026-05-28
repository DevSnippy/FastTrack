package com.darkrockstudios.apps.fasttrack.data.food

import com.darkrockstudios.apps.fasttrack.data.database.FoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class FoodLogRepositoryImpl(
	private val datasource: FoodLogDatasource
) : FoodLogRepository {

	override fun loadAll(): Flow<List<FoodLogEntry>> = datasource.loadAll().map { entries ->
		entries.map { it.toFoodLogEntry() }
	}

	override fun addEntry(description: String, timestamp: Long) {
		datasource.insert(FoodEntry(description = description, timestamp = timestamp))
	}

	override fun delete(entry: FoodLogEntry): Boolean {
		return datasource.deleteByUid(entry.id) > 0
	}

	override fun updateCalories(id: Int, calories: Int): Boolean {
		return datasource.updateCalories(id, calories) > 0
	}

	private fun FoodEntry.toFoodLogEntry(): FoodLogEntry {
		val instant = Instant.fromEpochMilliseconds(timestamp)
		val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
		return FoodLogEntry(
			id = uid,
			description = description,
			time = localDateTime,
			calories = calories,
		)
	}
}
