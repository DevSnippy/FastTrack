package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["dayOfWeek"])
data class WeeklyPlanEntry(
	@ColumnInfo val dayOfWeek: Int,    // 1=Mon … 7=Sun
	@ColumnInfo val scheduleId: Int?,  // null = rest day
)
