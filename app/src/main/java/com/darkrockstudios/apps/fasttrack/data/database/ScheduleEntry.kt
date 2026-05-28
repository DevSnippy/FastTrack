package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScheduleEntry(
	@PrimaryKey(autoGenerate = true) val uid: Int = 0,
	@ColumnInfo val name: String,
	@ColumnInfo val fastingHours: Int,
	@ColumnInfo val eatingWindowHours: Int,
	@ColumnInfo val eatStartHour: Int,
	@ColumnInfo val eatStartMinute: Int,
	@ColumnInfo val isActive: Boolean = false,
)
