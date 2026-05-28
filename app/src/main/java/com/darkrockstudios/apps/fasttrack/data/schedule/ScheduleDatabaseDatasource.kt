package com.darkrockstudios.apps.fasttrack.data.schedule

import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.ScheduleEntry
import kotlinx.coroutines.flow.Flow

class ScheduleDatabaseDatasource(
	private val database: AppDatabase
) : ScheduleDatasource {
	override fun loadAll(): Flow<List<ScheduleEntry>> = database.scheduleDao().loadAll()
	override fun insert(entry: ScheduleEntry): Long = database.scheduleDao().insert(entry)
	override fun deleteByUid(uid: Int): Int = database.scheduleDao().deleteByUid(uid)
	override fun deactivateAll(): Int = database.scheduleDao().deactivateAll()
	override fun activate(uid: Int): Int = database.scheduleDao().activate(uid)
}
