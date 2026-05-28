package com.darkrockstudios.apps.fasttrack.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.food.FoodLogEntry
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import com.darkrockstudios.apps.fasttrack.utils.formatAs
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FoodScreen(
	contentPaddingValues: PaddingValues = PaddingValues(0.dp),
	viewModel: IFoodViewModel = koinViewModel<FoodViewModel>(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val lifecycleOwner = LocalLifecycleOwner.current
	val context = LocalContext.current

	LaunchedEffect(Unit) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			viewModel.loadEntries()
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		val direction = androidx.compose.ui.platform.LocalLayoutDirection.current
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
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Spacer(Modifier.height(4.dp))

			uiState.totalCalories?.let { total ->
				ElevatedCard(modifier = Modifier.fillMaxWidth()) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
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
					enabled = uiState.entries.isNotEmpty(),
				) {
					Icon(
						imageVector = Icons.Default.ContentCopy,
						contentDescription = null,
						modifier = Modifier.size(16.dp),
					)
					Spacer(Modifier.width(6.dp))
					Text(
						text = stringResource(R.string.food_copy_prompt_button),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}

				OutlinedButton(
					onClick = { viewModel.showPasteDialog() },
					modifier = Modifier.weight(1f),
					enabled = uiState.entries.isNotEmpty(),
				) {
					Icon(
						imageVector = Icons.Default.ContentPaste,
						contentDescription = null,
						modifier = Modifier.size(16.dp),
					)
					Spacer(Modifier.width(6.dp))
					Text(
						text = stringResource(R.string.food_paste_response_button),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}

			if (uiState.entries.isEmpty()) {
				Box(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth(),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(R.string.food_empty_state),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			} else {
				LazyColumn(
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(
						bottom = contentPaddingValues.calculateBottomPadding() + 88.dp,
					),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					items(uiState.entries, key = { it.id }) { entry ->
						FoodEntryCard(
							entry = entry,
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
				.padding(
					bottom = contentPaddingValues.calculateBottomPadding() + 16.dp,
					end = 16.dp,
					start = 16.dp,
					top = 16.dp,
				),
		) {
			Icon(
				imageVector = Icons.Default.Add,
				contentDescription = stringResource(R.string.food_add_title),
			)
		}

		if (uiState.showAddDialog) {
			AddFoodDialog(
				onDismiss = { viewModel.hideAddDialog() },
				onAdd = { description, timestamp ->
					viewModel.addEntry(description, timestamp)
					viewModel.hideAddDialog()
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
private fun FoodEntryCard(
	entry: FoodLogEntry,
	onDelete: (FoodLogEntry) -> Unit,
) {
	ElevatedCard(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = entry.description,
					style = MaterialTheme.typography.bodyLarge,
					maxLines = 3,
					overflow = TextOverflow.Ellipsis,
				)
				Spacer(Modifier.height(4.dp))
				Text(
					text = entry.time.formatAs("h:mm a, MMM d"),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}

			Spacer(Modifier.width(8.dp))

			if (entry.calories != null) {
				SuggestionChip(
					onClick = {},
					label = {
						Text(
							text = "${entry.calories} kcal",
							style = MaterialTheme.typography.labelMedium,
						)
					},
				)
				Spacer(Modifier.width(4.dp))
			}

			IconButton(onClick = { onDelete(entry) }) {
				Icon(
					imageVector = Icons.Default.Delete,
					contentDescription = stringResource(R.string.menu_delete),
					tint = MaterialTheme.colorScheme.error,
				)
			}
		}
	}
}
