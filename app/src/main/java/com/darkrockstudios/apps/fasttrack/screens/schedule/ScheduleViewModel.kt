package com.darkrockstudios.apps.fasttrack.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleItem
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
	private val repository: ScheduleRepository,
) : ViewModel(), IScheduleViewModel {

	private val _uiState = MutableStateFlow(IScheduleViewModel.ScheduleUiState())
	override val uiState: StateFlow<IScheduleViewModel.ScheduleUiState> = _uiState.asStateFlow()

	override fun loadSchedules() {
		viewModelScope.launch {
			repository.loadAll().collect { schedules ->
				_uiState.update {
					it.copy(
						schedules = schedules,
						activeSchedule = schedules.firstOrNull { s -> s.isActive },
					)
				}
			}
		}
	}

	override fun addSchedule(
		name: String,
		fastingHours: Int,
		eatingWindowHours: Int,
		eatStartHour: Int,
		eatStartMinute: Int,
	) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.addSchedule(name, fastingHours, eatingWindowHours, eatStartHour, eatStartMinute)
		}
	}

	override fun deleteSchedule(item: ScheduleItem) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.delete(item)
		}
	}

	override fun activateSchedule(item: ScheduleItem) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.setActive(item)
		}
	}

	override fun deactivateSchedule(item: ScheduleItem) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.deactivate(item)
		}
	}

	override fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
	override fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
}
