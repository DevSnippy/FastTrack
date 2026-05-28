package com.darkrockstudios.apps.fasttrack.data.food

import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FoodEntry
import kotlinx.coroutines.flow.Flow

class FoodLogDatabaseDatasource(
	private val database: AppDatabase
) : FoodLogDatasource {
	override fun loadAll(): Flow<List<FoodEntry>> = database.foodDao().loadAll()
	override fun insert(entry: FoodEntry): Long = database.foodDao().insert(entry)
	override fun deleteByUid(uid: Int): Int = database.foodDao().deleteByUid(uid)
	override fun updateCalories(uid: Int, calories: Int): Int = database.foodDao().updateCalories(uid, calories)
}
