package com.darkrockstudios.apps.fasttrack.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.darkrockstudios.apps.fasttrack.data.schedule.SCHEDULE_TEMPLATES
import com.darkrockstudios.apps.fasttrack.data.schedule.ScheduleTemplate
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.utils.shouldUse24HourFormat

private enum class AddStep { PickTemplate, Configure }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
	onDismiss: () -> Unit,
	onAdd: (name: String, fastingHours: Int, eatingWindowHours: Int, eatStartHour: Int, eatStartMinute: Int) -> Unit,
) {
	var step by remember { mutableStateOf(AddStep.PickTemplate) }
	var selectedTemplate by remember { mutableStateOf<ScheduleTemplate?>(null) }

	var name by remember { mutableStateOf("") }
	var customFastHours by remember { mutableStateOf("16") }
	val use24Hour = shouldUse24HourFormat(getContext())
	val timePickerState = rememberTimePickerState(
		initialHour = 12,
		initialMinute = 0,
		is24Hour = use24Hour,
	)

	LaunchedEffect(selectedTemplate) {
		selectedTemplate?.let { t ->
			name = if (t.isCustom) "" else "${t.label} — ${t.popularName}"
			customFastHours = t.fastingHours.toString()
		}
	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Card(
			modifier = Modifier
				.widthIn(max = 560.dp)
				.heightIn(max = 700.dp)
				.padding(horizontal = 24.dp),
		) {
			Column(modifier = Modifier.padding(16.dp)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(
							if (step == AddStep.PickTemplate) R.string.schedule_pick_template_title
							else R.string.schedule_configure_title
						),
						style = MaterialTheme.typography.headlineSmall,
					)
					IconButton(onClick = onDismiss) {
						Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_button_content_description))
					}
				}

				Spacer(Modifier.height(8.dp))

				when (step) {
					AddStep.PickTemplate -> {
						LazyColumn(
							modifier = Modifier.weight(1f),
							verticalArrangement = Arrangement.spacedBy(8.dp),
						) {
							items(SCHEDULE_TEMPLATES, key = { it.id }) { template ->
								TemplateCard(
									template = template,
									onClick = {
										selectedTemplate = template
										step = AddStep.Configure
									},
								)
							}
						}

						Spacer(Modifier.height(8.dp))

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End,
						) {
							TextButton(onClick = onDismiss) {
								Text(stringResource(R.string.cancel_button))
							}
						}
					}

					AddStep.Configure -> {
						Column(
							modifier = Modifier
								.weight(1f)
								.verticalScroll(rememberScrollState()),
							verticalArrangement = Arrangement.spacedBy(12.dp),
						) {
							OutlinedTextField(
								value = name,
								onValueChange = { name = it },
								label = { Text(stringResource(R.string.schedule_name_label)) },
								modifier = Modifier.fillMaxWidth(),
								singleLine = true,
							)

							if (selectedTemplate?.isCustom == true) {
								OutlinedTextField(
									value = customFastHours,
									onValueChange = { v ->
										if (v.isEmpty() || v.toIntOrNull() != null) customFastHours = v
									},
									label = { Text(stringResource(R.string.schedule_fasting_hours_label)) },
									modifier = Modifier.fillMaxWidth(),
									keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
									singleLine = true,
									supportingText = {
										val h = customFastHours.toIntOrNull() ?: 0
										val eat = 24 - h
										if (h in 1..23) Text(stringResource(R.string.schedule_eating_window_computed, eat))
									},
								)
							}

							Text(
								text = stringResource(R.string.schedule_eat_start_label),
								style = MaterialTheme.typography.labelMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)

							TimeInput(
								state = timePickerState,
								modifier = Modifier.align(Alignment.CenterHorizontally),
							)
						}

						Spacer(Modifier.height(8.dp))

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
						) {
							TextButton(onClick = { step = AddStep.PickTemplate }) {
								Text(stringResource(R.string.previous_button))
							}
							Row {
								TextButton(onClick = onDismiss) {
									Text(stringResource(R.string.cancel_button))
								}
								Spacer(Modifier.width(8.dp))
								val fastHours = if (selectedTemplate?.isCustom == true) {
									customFastHours.toIntOrNull() ?: 0
								} else {
									selectedTemplate?.fastingHours ?: 0
								}
								val eatHours = 24 - fastHours
								Button(
									onClick = {
										if (name.isNotBlank() && fastHours in 1..23) {
											onAdd(
												name.trim(),
												fastHours,
												eatHours,
												timePickerState.hour,
												timePickerState.minute,
											)
										}
									},
									enabled = name.isNotBlank() && fastHours in 1..23,
								) {
									Text(stringResource(R.string.schedule_save_button))
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun TemplateCard(
	template: ScheduleTemplate,
	onClick: () -> Unit,
) {
	ElevatedCard(
		onClick = onClick,
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column(modifier = Modifier.weight(1f)) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					Text(
						text = template.label,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.primary,
					)
					if (template.popularName.isNotEmpty() && template.popularName != template.label) {
						Text(
							text = template.popularName,
							style = MaterialTheme.typography.labelMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}
				Spacer(Modifier.height(2.dp))
				Text(
					text = template.description,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				if (!template.isCustom) {
					Spacer(Modifier.height(4.dp))
					Text(
						text = "Fast ${template.fastingHours}h  ·  Eat ${template.eatingWindowHours}h",
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.tertiary,
					)
				}
			}
		}
	}
}
