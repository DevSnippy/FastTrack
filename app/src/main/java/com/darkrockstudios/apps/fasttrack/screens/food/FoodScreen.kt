package com.darkrockstudios.apps.fasttrack.screens.food

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogEntry
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import com.darkrockstudios.apps.fasttrack.utils.formatAs
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FoodScreen(
	contentPaddingValues: PaddingValues = PaddingValues(0.dp),
	viewModel: IFoodViewModel = koinViewModel<FoodViewModel>(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val lifecycleOwner = LocalLifecycleOwner.current
	val context = LocalContext.current

	val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

	var showDatePicker by remember { mutableStateOf(false) }
	val datePickerState = rememberDatePickerState()

	if (showDatePicker) {
		DatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			confirmButton = {
				TextButton(onClick = {
					datePickerState.selectedDateMillis?.let { ms ->
						val picked = kotlinx.datetime.Instant.fromEpochMilliseconds(ms)
							.toLocalDateTime(kotlinx.datetime.TimeZone.UTC).date
						viewModel.selectDate(picked)
					}
					showDatePicker = false
				}) { Text(stringResource(R.string.done_button)) }
			},
			dismissButton = {
				TextButton(onClick = { showDatePicker = false }) {
					Text(stringResource(R.string.cancel_button))
				}
			},
		) {
			DatePicker(state = datePickerState)
		}
	}

	LaunchedEffect(Unit) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			viewModel.loadEntries()
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
			verticalArrangement = Arrangement.spacedBy(10.dp),
		) {
			Spacer(Modifier.height(4.dp))

			// Day navigation bar
			DayNavigationBar(
				selectedDate = uiState.selectedDate ?: today,
				today = today,
				onPrev = { viewModel.selectPrevDay() },
				onNext = { viewModel.selectNextDay() },
				onPickDate = { showDatePicker = true },
			)

			// Calories summary
			uiState.totalCalories?.let { total ->
				ElevatedCard(modifier = Modifier.fillMaxWidth()) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp, vertical = 12.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Text(
							text = stringResource(R.string.food_total_calories_label),
							style = MaterialTheme.typography.labelLarge,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
						Text(
							text = stringResource(R.string.food_total_calories_value, total),
							style = MaterialTheme.typography.headlineMedium,
						)
					}
				}
			}

			// AI action buttons — only relevant when there are entries
			if (uiState.entries.isNotEmpty()) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					OutlinedButton(
						onClick = {
							val prompt = viewModel.buildAiPrompt()
							if (prompt.isNotEmpty()) {
								val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
								clipboard.setPrimaryClip(ClipData.newPlainText("AI Prompt", prompt))
							}
						},
						modifier = Modifier.weight(1f),
					) {
						Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
						Spacer(Modifier.width(6.dp))
						Text(stringResource(R.string.food_copy_prompt_button), maxLines = 1, overflow = TextOverflow.Ellipsis)
					}

					OutlinedButton(
						onClick = { viewModel.showPasteDialog() },
						modifier = Modifier.weight(1f),
					) {
						Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
						Spacer(Modifier.width(6.dp))
						Text(stringResource(R.string.food_paste_response_button), maxLines = 1, overflow = TextOverflow.Ellipsis)
					}
				}
			}

			if (uiState.entries.isEmpty()) {
				Box(
					modifier = Modifier.weight(1f).fillMaxWidth(),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(R.string.food_empty_day_state),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center,
					)
				}
			} else {
				LazyColumn(
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(bottom = contentPaddingValues.calculateBottomPadding() + 88.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					items(uiState.entries, key = { it.id }) { entry ->
						FoodEntryCard(
							entry = entry,
							onEdit = { viewModel.showEditDialog(it) },
							onDelete = { viewModel.deleteEntry(it) },
						)
					}
				}
			}
		}

		FloatingActionButton(
			onClick = { viewModel.showAddDialog() },
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(bottom = contentPaddingValues.calculateBottomPadding() + 16.dp, end = 16.dp),
		) {
			Icon(Icons.Default.Add, contentDescription = stringResource(R.string.food_add_title))
		}

		if (uiState.showAddDialog) {
			AddFoodDialog(
				onDismiss = { viewModel.hideAddDialog() },
				onSave = { description, timestamp, calories ->
					viewModel.addEntry(description, timestamp, calories)
					viewModel.hideAddDialog()
				},
			)
		}

		if (uiState.entryToEdit != null) {
			AddFoodDialog(
				entryToEdit = uiState.entryToEdit,
				onDismiss = { viewModel.hideEditDialog() },
				onSave = { description, timestamp, calories ->
					viewModel.updateEntry(uiState.entryToEdit!!.id, description, timestamp, calories)
					viewModel.hideEditDialog()
				},
			)
		}

		if (uiState.showPasteDialog) {
			PasteCaloriesDialog(
				onDismiss = { viewModel.hidePasteDialog() },
				onApply = { json -> viewModel.applyCalorieEstimates(json) },
			)
		}
	}
}

@Composable
private fun DayNavigationBar(
	selectedDate: LocalDate,
	today: LocalDate,
	onPrev: () -> Unit,
	onNext: () -> Unit,
	onPickDate: () -> Unit,
) {
	val isToday = selectedDate == today
	val yesterday = remember(today) { today.minus(1, DateTimeUnit.DAY) }
	val label = when (selectedDate) {
		today -> stringResource(R.string.food_day_today)
		yesterday -> stringResource(R.string.food_day_yesterday)
		else -> selectedDate.formatDate()
	}

	ElevatedCard(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp, vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(onClick = onPrev) {
				Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_button))
			}

			Text(
				text = label,
				style = MaterialTheme.typography.titleMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.weight(1f),
			)

			IconButton(onClick = onPickDate) {
				Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.edit_date))
			}

			IconButton(onClick = onNext, enabled = !isToday) {
				Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_button))
			}
		}
	}
}

private fun LocalDate.formatDate(): String {
	val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
	return "${months[monthNumber - 1]} $dayOfMonth, $year"
}

@Composable
private fun FoodEntryCard(
	entry: FoodLogEntry,
	onEdit: (FoodLogEntry) -> Unit,
	onDelete: (FoodLogEntry) -> Unit,
) {
	ElevatedCard(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(entry.description, style = MaterialTheme.typography.bodyLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
				Spacer(Modifier.height(4.dp))
				Text(
					entry.time.formatAs("h:mm a"),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}

			Spacer(Modifier.width(8.dp))

			if (entry.calories != null) {
				SuggestionChip(
					onClick = {},
					label = { Text("${entry.calories} kcal", style = MaterialTheme.typography.labelMedium) },
				)
				Spacer(Modifier.width(4.dp))
			}

			IconButton(onClick = { onEdit(entry) }) {
				Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.menu_edit), tint = MaterialTheme.colorScheme.primary)
			}
			IconButton(onClick = { onDelete(entry) }) {
				Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.menu_delete), tint = MaterialTheme.colorScheme.error)
			}
		}
	}
}
