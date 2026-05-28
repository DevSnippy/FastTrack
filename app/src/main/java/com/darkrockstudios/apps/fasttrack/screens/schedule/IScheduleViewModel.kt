package com.darkrockstudios.apps.fasttrack.screens.schedule

import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleItem
import com.darkrockstudios.apps.fasttrack.data.schedule.WeeklyPlanDay
import kotlinx.coroutines.flow.StateFlow

interface IScheduleViewModel {
	data class ScheduleUiState(
		val schedules: List<ScheduleItem> = emptyList(),
		val activeSchedule: ScheduleItem? = null,
		val showAddDialog: Boolean = false,
		val weeklyPlan: List<WeeklyPlanDay> = emptyList(),
		val dayToEdit: Int? = null,
	)

	val uiState: StateFlow<ScheduleUiState>

	fun loadSchedules()
	fun addSchedule(name: String, fastingHours: Int, eatingWindowHours: Int, eatStartHour: Int, eatStartMinute: Int)
	fun deleteSchedule(item: ScheduleItem)
	fun activateSchedule(item: ScheduleItem)
	fun deactivateSchedule(item: ScheduleItem)
	fun showAddDialog()
	fun hideAddDialog()
	fun setDaySchedule(dayOfWeek: Int, scheduleId: Int?)
	fun showDayPicker(dayOfWeek: Int)
	fun hideDayPicker()
}
