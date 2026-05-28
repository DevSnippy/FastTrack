package com.darkrockstudios.apps.fasttrack.data.schedule

data class WeeklyPlanDay(
	val dayOfWeek: Int, // 1=Mon … 7=Sun
	val schedule: ScheduleItem? = null,
) {
	val dayName: String get() = when (dayOfWeek) {
		1 -> "Monday"
		2 -> "Tuesday"
		3 -> "Wednesday"
		4 -> "Thursday"
		5 -> "Friday"
		6 -> "Saturday"
		7 -> "Sunday"
		else -> ""
	}
	val dayNameShort: String get() = dayName.take(3)
}
