package com.darkrockstudios.apps.fasttrack.di

import androidx.room.Room
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastDataSource
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastPreferencesDataSource
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogDatabaseDatasource
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogDatasource
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogRepository
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleDatabaseDatasource
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleDatasource
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleRepository
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogDatabaseDatasource
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogDatasource
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsPreferencesDatasource
import com.darkrockstudios.apps.fasttrack.screens.fasting.FastingViewModel
import com.darkrockstudios.apps.fasttrack.screens.fasting.IFastingViewModel
import com.darkrockstudios.apps.fasttrack.screens.food.FoodViewModel
import com.darkrockstudios.apps.fasttrack.screens.food.IFoodViewModel
import com.darkrockstudios.apps.fasttrack.screens.schedule.IScheduleViewModel
import com.darkrockstudios.apps.fasttrack.screens.schedule.ScheduleViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.ILogViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.LogViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.manualadd.IManualAddViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.manualadd.ManualAddViewModel
import com.darkrockstudios.apps.fasttrack.screens.profile.IProfileViewModel
import com.darkrockstudios.apps.fasttrack.screens.profile.ProfileViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

val mainModule = module {
	single {
		Room.databaseBuilder(
			get(),
			AppDatabase::class.java,
			"app-database"
		).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4).build()
	}

	single { Clock.System } bind Clock::class

	singleOf(::SettingsPreferencesDatasource) bind SettingsDatasource::class

	singleOf(::ActiveFastPreferencesDataSource) bind ActiveFastDataSource::class
	singleOf(::ActiveFastRepositoryImpl) bind ActiveFastRepository::class

	singleOf(::FastingLogDatabaseDatasource) bind FastingLogDatasource::class
	singleOf(::FastingLogRepositoryImpl) bind FastingLogRepository::class

	singleOf(::FoodLogDatabaseDatasource) bind FoodLogDatasource::class
	singleOf(::FoodLogRepositoryImpl) bind FoodLogRepository::class

	singleOf(::ScheduleDatabaseDatasource) bind ScheduleDatasource::class
	singleOf(::ScheduleRepositoryImpl) bind ScheduleRepository::class

	viewModelOf(::FastingViewModel) bind IFastingViewModel::class
	viewModelOf(::LogViewModel) bind ILogViewModel::class
	viewModelOf(::ProfileViewModel) bind IProfileViewModel::class
	viewModelOf(::ManualAddViewModel) bind IManualAddViewModel::class
	viewModelOf(::FoodViewModel) bind IFoodViewModel::class
	viewModelOf(::ScheduleViewModel) bind IScheduleViewModel::class
}
