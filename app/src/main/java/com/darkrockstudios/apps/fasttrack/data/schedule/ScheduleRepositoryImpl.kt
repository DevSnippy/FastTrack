package com.darkrockstudios.apps.fasttrack.data.schedule

import com.darkrockstudios.apps.fasttrack.data.database.ScheduleEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepositoryImpl(
	private val datasource: ScheduleDatasource
) : ScheduleRepository {

	override fun loadAll(): Flow<List<ScheduleItem>> = datasource.loadAll().map { entries ->
		entries.map { it.toScheduleItem() }
	}

	override fun addSchedule(
		name: String,
		fastingHours: Int,
		eatingWindowHours: Int,
		eatStartHour: Int,
		eatStartMinute: Int,
	) {
		datasource.insert(
			ScheduleEntry(
				name = name,
				fastingHours = fastingHours,
				eatingWindowHours = eatingWindowHours,
				eatStartHour = eatStartHour,
				eatStartMinute = eatStartMinute,
			)
		)
	}

	override fun delete(item: ScheduleItem): Boolean {
		return datasource.deleteByUid(item.id) > 0
	}

	override fun setActive(item: ScheduleItem): Boolean {
		datasource.deactivateAll()
		return datasource.activate(item.id) > 0
	}

	override fun deactivate(item: ScheduleItem): Boolean {
		return datasource.deactivateAll() >= 0
	}

	override fun loadWeeklyPlanMap(): Flow<Map<Int, Int?>> {
		return datasource.loadWeeklyPlan().map { entries ->
			entries.associate { it.dayOfWeek to it.scheduleId }
		}
	}

	override fun setDaySchedule(dayOfWeek: Int, scheduleId: Int?) {
		datasource.setDaySchedule(dayOfWeek, scheduleId)
	}

	private fun ScheduleEntry.toScheduleItem() = ScheduleItem(
		id = uid,
		name = name,
		fastingHours = fastingHours,
		eatingWindowHours = eatingWindowHours,
		eatStartHour = eatStartHour,
		eatStartMinute = eatStartMinute,
		isActive = isActive,
	)
}
