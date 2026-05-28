package com.darkrockstudios.apps.fasttrack.data.schedule

import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
	fun loadAll(): Flow<List<ScheduleItem>>
	fun addSchedule(name: String, fastingHours: Int, eatingWindowHours: Int, eatStartHour: Int, eatStartMinute: Int)
	fun delete(item: ScheduleItem): Boolean
	fun setActive(item: ScheduleItem): Boolean
	fun deactivate(item: ScheduleItem): Boolean
}
