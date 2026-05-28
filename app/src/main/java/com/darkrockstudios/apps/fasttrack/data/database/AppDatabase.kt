package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FastEntry::class, FoodEntry::class, ScheduleEntry::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
	abstract fun fastDao(): FastEntryDao
	abstract fun foodDao(): FoodEntryDao
	abstract fun scheduleDao(): ScheduleEntryDao

	companion object {
		val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL(
					"CREATE TABLE IF NOT EXISTS `FoodEntry` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `calories` INTEGER)"
				)
			}
		}

		val MIGRATION_2_3 = object : Migration(2, 3) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL(
					"CREATE TABLE IF NOT EXISTS `ScheduleEntry` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `fastingHours` INTEGER NOT NULL, `eatingWindowHours` INTEGER NOT NULL, `eatStartHour` INTEGER NOT NULL, `eatStartMinute` INTEGER NOT NULL, `isActive` INTEGER NOT NULL DEFAULT 0)"
				)
			}
		}
	}
}
