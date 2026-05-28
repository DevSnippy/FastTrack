package com.darkrockstudios.apps.fasttrack.screens.food

import com.darkrockstudios.apps.fasttrack.data.food.FoodLogEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

interface IFoodViewModel {
	data class FoodUiState(
		val entries: List<FoodLogEntry> = emptyList(),
		val totalCalories: Int? = null,
		val selectedDate: LocalDate? = null,
		val showAddDialog: Boolean = false,
		val showPasteDialog: Boolean = false,
		val entryToEdit: FoodLogEntry? = null,
	)

	val uiState: StateFlow<FoodUiState>

	fun loadEntries()
	fun addEntry(description: String, timestamp: Long, calories: Int? = null)
	fun updateEntry(id: Int, description: String, timestamp: Long, calories: Int?)
	fun deleteEntry(entry: FoodLogEntry)
	fun buildAiPrompt(): String
	fun applyCalorieEstimates(json: String): Boolean
	fun selectDate(date: LocalDate)
	fun selectPrevDay()
	fun selectNextDay()
	fun showAddDialog()
	fun hideAddDialog()
	fun showEditDialog(entry: FoodLogEntry)
	fun hideEditDialog()
	fun showPasteDialog()
	fun hidePasteDialog()
}
