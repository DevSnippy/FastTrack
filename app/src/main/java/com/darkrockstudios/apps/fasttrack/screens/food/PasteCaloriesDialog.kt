package com.darkrockstudios.apps.fasttrack.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.R

@Composable
fun PasteCaloriesDialog(
	onDismiss: () -> Unit,
	onApply: (json: String) -> Boolean,
) {
	var jsonText by remember { mutableStateOf("") }
	var hasError by remember { mutableStateOf(false) }

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
				Text(
					text = stringResource(R.string.food_paste_title),
					style = MaterialTheme.typography.headlineSmall,
				)
				Text(
					text = stringResource(R.string.food_paste_description),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				OutlinedTextField(
					value = jsonText,
					onValueChange = {
						jsonText = it
						hasError = false
					},
					label = { Text(stringResource(R.string.food_paste_hint)) },
					modifier = Modifier.fillMaxWidth(),
					minLines = 4,
					isError = hasError,
					supportingText = if (hasError) {
						{ Text(stringResource(R.string.food_paste_error)) }
					} else null,
				)
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End,
				) {
					TextButton(onClick = onDismiss) {
						Text(stringResource(R.string.cancel_button))
					}
					Spacer(Modifier.width(8.dp))
					Button(
						onClick = {
							val success = onApply(jsonText)
							if (success) onDismiss() else hasError = true
						},
						enabled = jsonText.isNotBlank(),
					) {
						Text(stringResource(R.string.food_paste_apply_button))
					}
				}
			}
		}
	}
}
