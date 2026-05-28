package com.darkrockstudios.apps.fasttrack.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.utils.shouldUse24HourFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddFoodDialog(
	onDismiss: () -> Unit,
	onAdd: (description: String, timestamp: Long) -> Unit,
) {
	val now = remember {
		Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
	}
	var description by remember { mutableStateOf("") }
	val use24Hour = shouldUse24HourFormat(getContext())
	val timePickerState = rememberTimePickerState(
		initialHour = now.hour,
		initialMinute = now.minute,
		is24Hour = use24Hour,
	)

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
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(R.string.food_add_title),
						style = MaterialTheme.typography.headlineSmall,
					)
					IconButton(onClick = onDismiss) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = stringResource(R.string.close_button_content_description),
						)
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

				Text(
					text = stringResource(R.string.food_time_label),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)

				TimeInput(
					state = timePickerState,
					modifier = Modifier.align(Alignment.CenterHorizontally),
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
								val today = now.date
								val selectedDateTime = LocalDateTime(
									date = today,
									time = LocalTime(
										hour = timePickerState.hour,
										minute = timePickerState.minute,
									),
								)
								val timestamp = selectedDateTime
									.toInstant(TimeZone.currentSystemDefault())
									.toEpochMilliseconds()
								onAdd(description.trim(), timestamp)
							}
						},
						enabled = description.isNotBlank(),
					) {
						Text(stringResource(R.string.food_add_button))
					}
				}
			}
		}
	}
}
