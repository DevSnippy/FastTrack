package com.darkrockstudios.apps.fasttrack.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogEntry
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogRepository
import com.darkrockstudios.apps.fasttrack.utils.formatAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.json.JSONArray
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FoodViewModel(
	private val repository: FoodLogRepository,
) : ViewModel(), IFoodViewModel {

	private val today: LocalDate
		get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

	private val _selectedDate = MutableStateFlow(today)

	private val _uiState = MutableStateFlow(IFoodViewModel.FoodUiState(selectedDate = today))
	override val uiState: StateFlow<IFoodViewModel.FoodUiState> = _uiState.asStateFlow()

	override fun loadEntries() {
		viewModelScope.launch {
			combine(repository.loadAll(), _selectedDate) { all, date ->
				val filtered = all.filter { it.time.date == date }
				val total = if (filtered.any { it.calories != null }) filtered.sumOf { it.calories ?: 0 } else null
				Triple(filtered, total, date)
			}.collect { (filtered, total, date) ->
				_uiState.update { it.copy(entries = filtered, totalCalories = total, selectedDate = date) }
			}
		}
	}

	override fun selectDate(date: LocalDate) {
		_selectedDate.value = date
	}

	override fun selectPrevDay() {
		_selectedDate.update { it.minus(1, DateTimeUnit.DAY) }
	}

	override fun selectNextDay() {
		_selectedDate.update { it.plus(1, DateTimeUnit.DAY) }
	}

	override fun addEntry(description: String, timestamp: Long, calories: Int?) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.addEntry(description, timestamp, calories)
		}
	}

	override fun updateEntry(id: Int, description: String, timestamp: Long, calories: Int?) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.updateEntry(id, description, timestamp, calories)
		}
	}

	override fun deleteEntry(entry: FoodLogEntry) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.delete(entry)
		}
	}

	override fun buildAiPrompt(): String {
		val entries = _uiState.value.entries
		if (entries.isEmpty()) return ""
		val itemLines = entries.joinToString("\n") { entry ->
			val timeStr = entry.time.formatAs("h:mm a, MMM d")
			"- [id:${entry.id}] [$timeStr] ${entry.description}"
		}
		return """
Please estimate the calories for each food item listed below.
Respond with ONLY a JSON array — no explanation, no markdown, just the array.
Use the id values exactly as shown.

Required format:
[{"id":1,"calories":350},{"id":2,"calories":200}]

Food items:
$itemLines
		""".trimIndent()
	}

	override fun applyCalorieEstimates(json: String): Boolean {
		return try {
			val array = JSONArray(json.trim())
			val updates = (0 until array.length()).map { i ->
				val obj = array.getJSONObject(i)
				obj.getInt("id") to obj.getInt("calories")
			}
			viewModelScope.launch(Dispatchers.IO) {
				updates.forEach { (id, calories) -> repository.updateCalories(id, calories) }
			}
			true
		} catch (e: Exception) {
			false
		}
	}

	override fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true, entryToEdit = null) }
	override fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
	override fun showEditDialog(entry: FoodLogEntry) = _uiState.update { it.copy(entryToEdit = entry, showAddDialog = false) }
	override fun hideEditDialog() = _uiState.update { it.copy(entryToEdit = null) }
	override fun showPasteDialog() = _uiState.update { it.copy(showPasteDialog = true) }
	override fun hidePasteDialog() = _uiState.update { it.copy(showPasteDialog = false) }
}
