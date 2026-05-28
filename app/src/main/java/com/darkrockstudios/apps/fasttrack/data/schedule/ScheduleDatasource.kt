package com.darkrockstudios.apps.fasttrack.data.schedule

import com.darkrockstudios.apps.fasttrack.data.database.ScheduleEntry
import kotlinx.coroutines.flow.Flow

interface ScheduleDatasource {
	fun loadAll(): Flow<List<ScheduleEntry>>
	fun insert(entry: ScheduleEntry): Long
	fun deleteByUid(uid: Int): Int
	fun deactivateAll(): Int
	fun activate(uid: Int): Int
}
