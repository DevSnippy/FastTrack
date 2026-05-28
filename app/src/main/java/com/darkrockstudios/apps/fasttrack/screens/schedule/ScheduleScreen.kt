package com.darkrockstudios.apps.fasttrack.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleItem
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

	LaunchedEffect(Unit) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			viewModel.loadSchedules()
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		val direction = LocalLayoutDirection.current
		Column(
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
		) {
			Spacer(Modifier.height(8.dp))

			uiState.activeSchedule?.let { active ->
				ActiveScheduleCard(
					schedule = active,
					onDeactivate = { viewModel.deactivateSchedule(active) },
				)
				Spacer(Modifier.height(16.dp))
			}

			if (uiState.schedules.isEmpty()) {
				Box(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth(),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(R.string.schedule_empty_state),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			} else {
				Text(
					text = stringResource(R.string.schedule_my_schedules_label),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Spacer(Modifier.height(8.dp))
				LazyColumn(
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(
						bottom = contentPaddingValues.calculateBottomPadding() + 88.dp,
					),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					items(uiState.schedules, key = { it.id }) { schedule ->
						ScheduleCard(
							schedule = schedule,
							onActivate = { viewModel.activateSchedule(schedule) },
							onDeactivate = { viewModel.deactivateSchedule(schedule) },
							onDelete = { viewModel.deleteSchedule(schedule) },
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
					start = 16.dp,
					top = 16.dp,
				),
		) {
			Icon(
				imageVector = Icons.Default.Add,
				contentDescription = stringResource(R.string.schedule_add_button),
			)
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
	}
}

@Composable
private fun ActiveScheduleCard(
	schedule: ScheduleItem,
	onDeactivate: () -> Unit,
) {
	ElevatedCard(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.elevatedCardColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer,
		),
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
				LabeledStat(
					label = stringResource(R.string.schedule_eat_window_label),
					value = "${schedule.eatingWindowHours}h",
				)
				LabeledStat(
					label = stringResource(R.string.schedule_fast_window_label),
					value = "${schedule.fastingHours}h",
				)
				LabeledStat(
					label = stringResource(R.string.schedule_eat_time_label),
					value = "${formatHour(schedule.eatStartHour, schedule.eatStartMinute)} – ${formatHour(schedule.eatEndHour, schedule.eatEndMinute)}",
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

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(10.dp)
			.clip(RoundedCornerShape(5.dp)),
	) {
		if (fastBeforeEat > 0) {
			Box(modifier = Modifier.weight(fastBeforeEat.toFloat()).fillMaxHeight().background(fastColor))
		}
		if (eatWindow > 0) {
			Box(modifier = Modifier.weight(eatWindow.toFloat()).fillMaxHeight().background(eatColor))
		}
		if (fastAfterEat > 0) {
			Box(modifier = Modifier.weight(fastAfterEat.toFloat()).fillMaxHeight().background(fastColor))
		}
	}

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text("12 AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
		Text("12 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
		Text("12 AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
	}
}

@Composable
private fun LabeledStat(label: String, value: String) {
	Column {
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
		)
		Text(
			text = value,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onPrimaryContainer,
		)
	}
}

@Composable
private fun ScheduleCard(
	schedule: ScheduleItem,
	onActivate: () -> Unit,
	onDeactivate: () -> Unit,
	onDelete: () -> Unit,
) {
	ElevatedCard(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			RadioButton(
				selected = schedule.isActive,
				onClick = if (schedule.isActive) onDeactivate else onActivate,
			)

			Spacer(Modifier.width(4.dp))

			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = schedule.name,
					style = MaterialTheme.typography.bodyLarge,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = stringResource(
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
				Icon(
					imageVector = Icons.Default.Delete,
					contentDescription = stringResource(R.string.menu_delete),
					tint = MaterialTheme.colorScheme.error,
				)
			}
		}
	}
}

private fun formatHour(hour: Int, minute: Int): String {
	val amPm = if (hour < 12) "AM" else "PM"
	val displayHour = when {
		hour == 0 -> 12
		hour > 12 -> hour - 12
		else -> hour
	}
	return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}
