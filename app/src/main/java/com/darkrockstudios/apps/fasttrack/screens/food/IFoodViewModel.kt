package com.darkrockstudios.apps.fasttrack.screens.food

import com.darkrockstudios.apps.fasttrack.data.food.FoodLogEntry
import kotlinx.coroutines.flow.StateFlow

interface IFoodViewModel {
	data class FoodUiState(
		val entries: List<FoodLogEntry> = emptyList(),
		val totalCalories: Int? = null,
		val showAddDialog: Boolean = false,
		val showPasteDialog: Boolean = false,
	)

	val uiState: StateFlow<FoodUiState>

	fun loadEntries()
	fun addEntry(description: String, timestamp: Long)
	fun deleteEntry(entry: FoodLogEntry)
	fun buildAiPrompt(): String
	fun applyCalorieEstimates(json: String): Boolean
	fun showAddDialog()
	fun hideAddDialog()
	fun showPasteDialog()
	fun hidePasteDialog()
}
