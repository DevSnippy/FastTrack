package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FastEntry::class, FoodEntry::class, ScheduleEntry::class, WeeklyPlanEntry::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
	abstract fun fastDao(): FastEntryDao
	abstract fun foodDao(): FoodEntryDao
	abstract fun scheduleDao(): ScheduleEntryDao
	abstract fun weeklyPlanDao(): WeeklyPlanDao

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

		val MIGRATION_3_4 = object : Migration(3, 4) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL(
					"CREATE TABLE IF NOT EXISTS `WeeklyPlanEntry` (`dayOfWeek` INTEGER NOT NULL, `scheduleId` INTEGER, PRIMARY KEY(`dayOfWeek`))"
				)
			}
		}
	}
}
