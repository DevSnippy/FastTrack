package com.darkrockstudios.apps.fasttrack.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleItem
import com.darkrockstudios.apps.fasttrack.data.schedule.WeeklyPlanDay
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

@Composable
fun ScheduleScreen(
	contentPaddingValues: PaddingValues = PaddingValues(0.dp),
	viewModel: IScheduleViewModel = koinViewModel<ScheduleViewModel>(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val lifecycleOwner = LocalLifecycleOwner.current

	// Tracks whether the "add new schedule" sub-flow is open from within the day picker
	var showAddScheduleForDay by remember { mutableStateOf(false) }
	LaunchedEffect(uiState.dayToEdit) {
		if (uiState.dayToEdit == null) showAddScheduleForDay = false
	}

	LaunchedEffect(Unit) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			viewModel.loadSchedules()
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		val direction = LocalLayoutDirection.current
		LazyColumn(
			modifier = Modifier
				.fillMaxHeight()
				.widthIn(max = MAX_COLUMN_WIDTH)
				.align(Alignment.Center)
				.padding(
					start = contentPaddingValues.calculateStartPadding(direction),
					end = contentPaddingValues.calculateEndPadding(direction),
				)
				.padding(horizontal = 16.dp)
				.padding(top = contentPaddingValues.calculateTopPadding()),
			contentPadding = PaddingValues(
				top = 8.dp,
				bottom = contentPaddingValues.calculateBottomPadding() + 88.dp,
			),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			uiState.activeSchedule?.let { active ->
				item(key = "active") {
					ActiveScheduleCard(
						schedule = active,
						onDeactivate = { viewModel.deactivateSchedule(active) },
					)
				}
			}

			item(key = "weekly_grid") {
				WeeklyPlanGrid(
					weeklyPlan = uiState.weeklyPlan,
					onDayClick = { viewModel.showDayPicker(it) },
				)
			}

			if (uiState.schedules.isNotEmpty()) {
				item(key = "schedules_header") {
					Text(
						text = stringResource(R.string.schedule_my_schedules_label),
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}

				items(uiState.schedules, key = { it.id }) { schedule ->
					ScheduleCard(
						schedule = schedule,
						onActivate = { viewModel.activateSchedule(schedule) },
						onDeactivate = { viewModel.deactivateSchedule(schedule) },
						onDelete = { viewModel.deleteSchedule(schedule) },
					)
				}
			} else {
				item(key = "empty") {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(200.dp),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = stringResource(R.string.schedule_empty_state),
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							textAlign = TextAlign.Center,
						)
					}
				}
			}
		}

		FloatingActionButton(
			onClick = { viewModel.showAddDialog() },
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(
					bottom = contentPaddingValues.calculateBottomPadding() + 16.dp,
					end = 16.dp,
				),
		) {
			Icon(Icons.Default.Add, contentDescription = stringResource(R.string.schedule_add_button))
		}

		if (uiState.showAddDialog) {
			AddScheduleDialog(
				onDismiss = { viewModel.hideAddDialog() },
				onAdd = { name, fastHours, eatHours, startHour, startMinute ->
					viewModel.addSchedule(name, fastHours, eatHours, startHour, startMinute)
					viewModel.hideAddDialog()
				},
			)
		}

		val dayToEdit = uiState.dayToEdit
		if (dayToEdit != null) {
			if (showAddScheduleForDay) {
				AddScheduleDialog(
					onDismiss = { showAddScheduleForDay = false },
					onAdd = { name, fastHours, eatHours, startHour, startMinute ->
						viewModel.addSchedule(name, fastHours, eatHours, startHour, startMinute)
						showAddScheduleForDay = false
					},
				)
			} else {
				DaySchedulePickerDialog(
					dayOfWeek = dayToEdit,
					currentScheduleId = uiState.weeklyPlan
						.firstOrNull { it.dayOfWeek == dayToEdit }
						?.schedule?.id,
					schedules = uiState.schedules,
					onApply = { scheduleId ->
						viewModel.setDaySchedule(dayToEdit, scheduleId)
						viewModel.hideDayPicker()
					},
					onAddNew = { showAddScheduleForDay = true },
					onDismiss = { viewModel.hideDayPicker() },
				)
			}
		}
	}
}

// ─── Weekly plan grid ────────────────────────────────────────────────────────

@Composable
private fun WeeklyPlanGrid(
	weeklyPlan: List<WeeklyPlanDay>,
	onDayClick: (Int) -> Unit,
) {
	ElevatedCard(modifier = Modifier.fillMaxWidth()) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
		) {
			Text(
				text = stringResource(R.string.schedule_weekly_plan_label),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
			Spacer(Modifier.height(10.dp))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
			) {
				weeklyPlan.forEach { day ->
					WeeklyDayCell(
						day = day,
						onClick = { onDayClick(day.dayOfWeek) },
						modifier = Modifier.weight(1f),
					)
				}
			}
		}
	}
}

@Composable
private fun WeeklyDayCell(
	day: WeeklyPlanDay,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val hasSchedule = day.schedule != null
	Card(
		onClick = onClick,
		modifier = modifier,
		colors = CardDefaults.cardColors(
			containerColor = if (hasSchedule)
				MaterialTheme.colorScheme.primaryContainer
			else
				MaterialTheme.colorScheme.surfaceVariant,
		),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 8.dp, horizontal = 2.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Text(
				text = day.dayNameShort,
				style = MaterialTheme.typography.labelSmall,
				color = if (hasSchedule)
					MaterialTheme.colorScheme.onPrimaryContainer
				else
					MaterialTheme.colorScheme.onSurfaceVariant,
			)
			Text(
				text = if (hasSchedule) "${day.schedule!!.fastingHours}h" else "—",
				style = MaterialTheme.typography.labelMedium,
				color = if (hasSchedule)
					MaterialTheme.colorScheme.primary
				else
					MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
				textAlign = TextAlign.Center,
			)
		}
	}
}

// ─── Day picker dialog ───────────────────────────────────────────────────────

@Composable
private fun DaySchedulePickerDialog(
	dayOfWeek: Int,
	currentScheduleId: Int?,
	schedules: List<ScheduleItem>,
	onApply: (Int?) -> Unit,
	onAddNew: () -> Unit,
	onDismiss: () -> Unit,
) {
	val dayName = WeeklyPlanDay(dayOfWeek).dayName
	var pendingId by remember(dayOfWeek, currentScheduleId) {
		mutableStateOf(currentScheduleId)
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.schedule_pick_day_title, dayName)) },
		text = {
			Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
				PickerRow(
					label = stringResource(R.string.schedule_rest_day),
					sublabel = null,
					selected = pendingId == null,
					onClick = { pendingId = null },
				)
				HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
				schedules.forEach { schedule ->
					PickerRow(
						label = schedule.name,
						sublabel = "Fast ${schedule.fastingHours}h  ·  Eat ${schedule.eatingWindowHours}h",
						selected = pendingId == schedule.id,
						onClick = { pendingId = schedule.id },
					)
				}
				HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
				TextButton(
					onClick = onAddNew,
					modifier = Modifier.fillMaxWidth(),
				) {
					Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
					Spacer(Modifier.width(6.dp))
					Text(stringResource(R.string.schedule_add_new_option))
				}
			}
		},
		confirmButton = {
			Button(onClick = { onApply(pendingId) }) {
				Text(stringResource(R.string.schedule_apply_button))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.cancel_button))
			}
		},
	)
}

@Composable
private fun PickerRow(
	label: String,
	sublabel: String?,
	selected: Boolean,
	onClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 2.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		RadioButton(selected = selected, onClick = onClick)
		Spacer(Modifier.width(4.dp))
		Column(modifier = Modifier.weight(1f)) {
			Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
			if (sublabel != null) {
				Text(sublabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
			}
		}
	}
}

// ─── Active schedule card ────────────────────────────────────────────────────

@Composable
private fun ActiveScheduleCard(
	schedule: ScheduleItem,
	onDeactivate: () -> Unit,
) {
	ElevatedCard(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Column {
					Text(
						text = stringResource(R.string.schedule_active_label),
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
					)
					Text(
						text = schedule.name,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onPrimaryContainer,
					)
				}
				TextButton(onClick = onDeactivate) {
					Text(
						text = stringResource(R.string.schedule_deactivate_button),
						color = MaterialTheme.colorScheme.onPrimaryContainer,
					)
				}
			}

			Spacer(Modifier.height(12.dp))
			ScheduleTimeline(schedule)
			Spacer(Modifier.height(12.dp))

			Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
				LabeledStat(stringResource(R.string.schedule_eat_window_label), "${schedule.eatingWindowHours}h")
				LabeledStat(stringResource(R.string.schedule_fast_window_label), "${schedule.fastingHours}h")
				LabeledStat(
					stringResource(R.string.schedule_eat_time_label),
					"${formatHour(schedule.eatStartHour, schedule.eatStartMinute)} – ${formatHour(schedule.eatEndHour, schedule.eatEndMinute)}",
				)
			}
		}
	}
}

@Composable
private fun ScheduleTimeline(schedule: ScheduleItem) {
	val fastColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
	val eatColor = MaterialTheme.colorScheme.primary
	val fastBeforeEat = schedule.eatStartHour
	val eatWindow = schedule.eatingWindowHours
	val fastAfterEat = (24 - fastBeforeEat - eatWindow).coerceAtLeast(0)

	Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
		if (fastBeforeEat > 0) Box(Modifier.weight(fastBeforeEat.toFloat()).fillMaxHeight().background(fastColor))
		if (eatWindow > 0) Box(Modifier.weight(eatWindow.toFloat()).fillMaxHeight().background(eatColor))
		if (fastAfterEat > 0) Box(Modifier.weight(fastAfterEat.toFloat()).fillMaxHeight().background(fastColor))
	}
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
		val labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
		Text("12 AM", style = MaterialTheme.typography.labelSmall, color = labelColor)
		Text("12 PM", style = MaterialTheme.typography.labelSmall, color = labelColor)
		Text("12 AM", style = MaterialTheme.typography.labelSmall, color = labelColor)
	}
}

@Composable
private fun LabeledStat(label: String, value: String) {
	Column {
		Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
		Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
	}
}

// ─── Schedule list card ──────────────────────────────────────────────────────

@Composable
private fun ScheduleCard(
	schedule: ScheduleItem,
	onActivate: () -> Unit,
	onDeactivate: () -> Unit,
	onDelete: () -> Unit,
) {
	ElevatedCard(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			RadioButton(
				selected = schedule.isActive,
				onClick = if (schedule.isActive) onDeactivate else onActivate,
			)
			Spacer(Modifier.width(4.dp))
			Column(modifier = Modifier.weight(1f)) {
				Text(schedule.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
				Text(
					stringResource(
						R.string.schedule_card_detail,
						schedule.fastingHours,
						schedule.eatingWindowHours,
						formatHour(schedule.eatStartHour, schedule.eatStartMinute),
						formatHour(schedule.eatEndHour, schedule.eatEndMinute),
					),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
			IconButton(onClick = onDelete) {
				Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.menu_delete), tint = MaterialTheme.colorScheme.error)
			}
		}
	}
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatHour(hour: Int, minute: Int): String {
	val amPm = if (hour < 12) "AM" else "PM"
	val displayHour = when {
		hour == 0 -> 12
		hour > 12 -> hour - 12
		else -> hour
	}
	return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}
