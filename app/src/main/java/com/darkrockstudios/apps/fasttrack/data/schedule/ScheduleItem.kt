package com.darkrockstudios.apps.fasttrack.data.schedule

data class ScheduleItem(
	val id: Int,
	val name: String,
	val fastingHours: Int,
	val eatingWindowHours: Int,
	val eatStartHour: Int,
	val eatStartMinute: Int,
	val isActive: Boolean,
) {
	val eatEndHour: Int get() = (eatStartHour + eatingWindowHours) % 24
	val eatEndMinute: Int get() = eatStartMinute
}
