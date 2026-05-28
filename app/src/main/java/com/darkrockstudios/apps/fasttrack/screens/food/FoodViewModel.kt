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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray

class FoodViewModel(
	private val repository: FoodLogRepository,
) : ViewModel(), IFoodViewModel {

	private val _uiState = MutableStateFlow(IFoodViewModel.FoodUiState())
	override val uiState: StateFlow<IFoodViewModel.FoodUiState> = _uiState.asStateFlow()

	override fun loadEntries() {
		viewModelScope.launch {
			repository.loadAll().collect { entries ->
				val total = if (entries.any { it.calories != null }) {
					entries.sumOf { it.calories ?: 0 }
				} else null
				_uiState.update { it.copy(entries = entries, totalCalories = total) }
			}
		}
	}

	override fun addEntry(description: String, timestamp: Long) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.addEntry(description, timestamp)
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
				updates.forEach { (id, calories) ->
					repository.updateCalories(id, calories)
				}
			}
			true
		} catch (e: Exception) {
			false
		}
	}

	override fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
	override fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
	override fun showPasteDialog() = _uiState.update { it.copy(showPasteDialog = true) }
	override fun hidePasteDialog() = _uiState.update { it.copy(showPasteDialog = false) }
}
