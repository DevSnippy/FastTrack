package com.darkrockstudios.apps.fasttrack.screens.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogEntry
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.utils.shouldUse24HourFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddFoodDialog(
	entryToEdit: FoodLogEntry? = null,
	defaultDate: LocalDate? = null,
	onDismiss: () -> Unit,
	onSave: (description: String, timestamp: Long, calories: Int?) -> Unit,
) {
	val isEditing = entryToEdit != null
	val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
	val initial = entryToEdit?.time ?: now

	var description by remember { mutableStateOf(entryToEdit?.description ?: "") }
	var caloriesText by remember { mutableStateOf(entryToEdit?.calories?.toString() ?: "") }
	var selectedDate by remember { mutableStateOf(defaultDate ?: initial.date) }
	var showDatePicker by remember { mutableStateOf(false) }

	val use24Hour = shouldUse24HourFormat(getContext())
	val timePickerState = rememberTimePickerState(
		initialHour = initial.hour,
		initialMinute = initial.minute,
		is24Hour = use24Hour,
	)

	val datePickerState = rememberDatePickerState(
		initialSelectedDateMillis = initial.date
			.atStartOfDayIn(TimeZone.UTC)
			.toEpochMilliseconds(),
	)

	if (showDatePicker) {
		DatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			confirmButton = {
				TextButton(onClick = {
					datePickerState.selectedDateMillis?.let { ms ->
						selectedDate = Instant.fromEpochMilliseconds(ms)
							.toLocalDateTime(TimeZone.UTC)
							.date
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

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Card(
			modifier = Modifier
				.widthIn(max = 500.dp)
				.padding(horizontal = 24.dp)
		) {
			Column(
				modifier = Modifier
					.padding(16.dp)
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(if (isEditing) R.string.food_edit_title else R.string.food_add_title),
						style = MaterialTheme.typography.headlineSmall,
					)
					IconButton(onClick = onDismiss) {
						Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_button_content_description))
					}
				}

				OutlinedTextField(
					value = description,
					onValueChange = { description = it },
					label = { Text(stringResource(R.string.food_description_hint)) },
					modifier = Modifier.fillMaxWidth(),
					singleLine = false,
					minLines = 2,
				)

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { showDatePicker = true }
						.padding(vertical = 4.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Column {
						Text(
							text = stringResource(R.string.food_date_label),
							style = MaterialTheme.typography.labelMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
						Text(
							text = selectedDate.toString(),
							style = MaterialTheme.typography.bodyLarge,
						)
					}
					Icon(
						imageVector = Icons.Default.Edit,
						contentDescription = stringResource(R.string.edit_date),
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(20.dp),
					)
				}

				HorizontalDivider()

				Text(
					text = stringResource(R.string.food_time_label),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)

				TimeInput(
					state = timePickerState,
					modifier = Modifier.align(Alignment.CenterHorizontally),
				)

				OutlinedTextField(
					value = caloriesText,
					onValueChange = { v -> if (v.isEmpty() || v.toIntOrNull() != null) caloriesText = v },
					label = { Text(stringResource(R.string.food_calories_hint)) },
					modifier = Modifier.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					singleLine = true,
					placeholder = { Text(stringResource(R.string.food_calories_placeholder)) },
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End,
					verticalAlignment = Alignment.CenterVertically,
				) {
					TextButton(onClick = onDismiss) {
						Text(stringResource(R.string.cancel_button))
					}
					Spacer(Modifier.width(8.dp))
					Button(
						onClick = {
							if (description.isNotBlank()) {
								val selectedDateTime = LocalDateTime(
									date = selectedDate,
									time = LocalTime(hour = timePickerState.hour, minute = timePickerState.minute),
								)
								val timestamp = selectedDateTime
									.toInstant(TimeZone.currentSystemDefault())
									.toEpochMilliseconds()
								onSave(description.trim(), timestamp, caloriesText.toIntOrNull())
							}
						},
						enabled = description.isNotBlank(),
					) {
						Text(stringResource(if (isEditing) R.string.manual_add_save_button else R.string.food_add_button))
					}
				}
			}
		}
	}
}
