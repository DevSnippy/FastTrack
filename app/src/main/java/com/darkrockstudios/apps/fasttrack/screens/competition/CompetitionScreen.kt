package com.darkrockstudios.apps.fasttrack.screens.competition

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.competition.LeaderboardEntry
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CompetitionScreen(
	contentPaddingValues: PaddingValues = PaddingValues(0.dp),
	viewModel: ICompetitionViewModel = koinViewModel<CompetitionViewModel>(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val lifecycleOwner = LocalLifecycleOwner.current
	val context = LocalContext.current

	LaunchedEffect(Unit) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			viewModel.init()
		}
	}

	val direction = LocalLayoutDirection.current
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(
				start = contentPaddingValues.calculateStartPadding(direction),
				end = contentPaddingValues.calculateEndPadding(direction),
				top = contentPaddingValues.calculateTopPadding(),
			),
	) {
		if (uiState.isLoading && !uiState.isAuthenticated) {
			CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
		} else if (!uiState.isAuthenticated) {
			AuthForm(
				uiState = uiState,
				onServerUrlChanged = viewModel::onServerUrlChanged,
				onUsernameChanged = viewModel::onUsernameChanged,
				onPasswordChanged = viewModel::onPasswordChanged,
				onLogin = viewModel::login,
				onRegister = viewModel::register,
				onClearError = viewModel::clearError,
			)
		} else {
			CompetitionDashboard(
				uiState = uiState,
				context = context,
				onAddFriendInputChanged = viewModel::onAddFriendInputChanged,
				onAddFriend = viewModel::addFriend,
				onRefresh = viewModel::refreshLeaderboard,
				onLogout = viewModel::logout,
				contentPaddingValues = contentPaddingValues,
			)
		}

		uiState.error?.let { err ->
			Snackbar(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.padding(16.dp),
				action = {
					TextButton(onClick = viewModel::clearError) { Text(stringResource(R.string.done_button)) }
				},
			) { Text(err) }
		}
	}
}

// ─── Auth form ────────────────────────────────────────────────────────────────

@Composable
private fun AuthForm(
	uiState: ICompetitionViewModel.CompetitionUiState,
	onServerUrlChanged: (String) -> Unit,
	onUsernameChanged: (String) -> Unit,
	onPasswordChanged: (String) -> Unit,
	onLogin: () -> Unit,
	onRegister: () -> Unit,
	onClearError: () -> Unit = {},
) {
	Column(
		modifier = Modifier
			.widthIn(max = MAX_COLUMN_WIDTH)
			.fillMaxHeight()
			.padding(24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(R.string.competition_title),
			style = MaterialTheme.typography.headlineMedium,
			textAlign = TextAlign.Center,
		)
		Spacer(Modifier.height(8.dp))
		Text(
			text = stringResource(R.string.competition_auth_subtitle),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
		)
		Spacer(Modifier.height(24.dp))

		OutlinedTextField(
			value = uiState.serverUrl,
			onValueChange = onServerUrlChanged,
			label = { Text(stringResource(R.string.competition_server_url_label)) },
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
		)
		Spacer(Modifier.height(12.dp))
		OutlinedTextField(
			value = uiState.usernameInput,
			onValueChange = onUsernameChanged,
			label = { Text(stringResource(R.string.competition_username_label)) },
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
		)
		Spacer(Modifier.height(12.dp))
		OutlinedTextField(
			value = uiState.passwordInput,
			onValueChange = onPasswordChanged,
			label = { Text(stringResource(R.string.competition_password_label)) },
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
			visualTransformation = PasswordVisualTransformation(),
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
			supportingText = { Text(stringResource(R.string.competition_password_hint)) },
		)
		Spacer(Modifier.height(24.dp))

		val canSubmit = uiState.usernameInput.isNotBlank() && uiState.passwordInput.isNotBlank()
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			OutlinedButton(
				onClick = onRegister,
				modifier = Modifier.weight(1f),
				enabled = canSubmit && !uiState.isLoading,
			) { Text(stringResource(R.string.competition_register_button)) }

			Button(
				onClick = onLogin,
				modifier = Modifier.weight(1f),
				enabled = canSubmit && !uiState.isLoading,
			) { Text(stringResource(R.string.competition_login_button)) }
		}

		if (uiState.isLoading) {
			Spacer(Modifier.height(16.dp))
			LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
		}

		uiState.error?.let { err ->
			Spacer(Modifier.height(12.dp))
			Card(
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(
					text = err,
					color = MaterialTheme.colorScheme.onErrorContainer,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.padding(12.dp),
				)
			}
		}
	}
}

// ─── Authenticated dashboard ──────────────────────────────────────────────────

@Composable
private fun CompetitionDashboard(
	uiState: ICompetitionViewModel.CompetitionUiState,
	context: Context,
	onAddFriendInputChanged: (String) -> Unit,
	onAddFriend: () -> Unit,
	onRefresh: () -> Unit,
	onLogout: () -> Unit,
	contentPaddingValues: PaddingValues,
) {
	LazyColumn(
		modifier = Modifier
			.widthIn(max = MAX_COLUMN_WIDTH)
			.fillMaxSize()
			.padding(horizontal = 16.dp),
		contentPadding = PaddingValues(
			top = 12.dp,
			bottom = contentPaddingValues.calculateBottomPadding() + 16.dp,
		),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		// Profile card
		item {
			ElevatedCard(
				modifier = Modifier.fillMaxWidth(),
				colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
			) {
				Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(
							text = "👋 ${uiState.username}",
							style = MaterialTheme.typography.titleLarge,
							color = MaterialTheme.colorScheme.onPrimaryContainer,
						)
						TextButton(onClick = onLogout) {
							Text(stringResource(R.string.competition_logout_button),
								color = MaterialTheme.colorScheme.onPrimaryContainer)
						}
					}
					Spacer(Modifier.height(8.dp))
					Text(
						text = stringResource(R.string.competition_friend_code_label),
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
					)
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp),
					) {
						Text(
							text = uiState.friendCode,
							style = MaterialTheme.typography.headlineMedium,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onPrimaryContainer,
						)
						IconButton(onClick = {
							val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
							cm.setPrimaryClip(ClipData.newPlainText("Friend Code", uiState.friendCode))
						}) {
							Icon(
								Icons.Default.ContentCopy,
								contentDescription = stringResource(R.string.competition_copy_code),
								tint = MaterialTheme.colorScheme.onPrimaryContainer,
							)
						}
					}
				}
			}
		}

		// Add friend
		item {
			ElevatedCard(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
					Text(
						stringResource(R.string.competition_add_friend_label),
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Spacer(Modifier.height(8.dp))
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						OutlinedTextField(
							value = uiState.addFriendInput,
							onValueChange = onAddFriendInputChanged,
							label = { Text(stringResource(R.string.competition_add_friend_hint)) },
							modifier = Modifier.weight(1f),
							singleLine = true,
						)
						Button(
							onClick = onAddFriend,
							enabled = uiState.addFriendInput.isNotBlank() && !uiState.isLoading,
						) {
							Text(stringResource(R.string.competition_add_button))
						}
					}
					if (uiState.addFriendSuccess) {
						Text(
							stringResource(R.string.competition_friend_added),
							style = MaterialTheme.typography.labelMedium,
							color = MaterialTheme.colorScheme.primary,
						)
					}
				}
			}
		}

		// Leaderboard header
		item {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					stringResource(R.string.competition_leaderboard_label),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				IconButton(onClick = onRefresh) {
					Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.competition_refresh))
				}
			}
		}

		if (uiState.leaderboard.isEmpty()) {
			item {
				Box(
					modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						stringResource(R.string.competition_leaderboard_empty),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center,
					)
				}
			}
		} else {
			itemsIndexed(uiState.leaderboard, key = { _, e -> e.username }) { index, entry ->
				LeaderboardCard(rank = index + 1, entry = entry)
			}
		}
	}
}

@Composable
private fun LeaderboardCard(rank: Int, entry: LeaderboardEntry) {
	val rankEmoji = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#$rank" }
	ElevatedCard(
		modifier = Modifier.fillMaxWidth(),
		colors = if (entry.isMe)
			CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
		else
			CardDefaults.elevatedCardColors(),
	) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = rankEmoji,
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.width(48.dp),
			)
			Column(modifier = Modifier.weight(1f)) {
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
					Text(entry.username, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
					if (entry.isMe) {
						SuggestionChip(onClick = {}, label = { Text("you", style = MaterialTheme.typography.labelSmall) })
					}
				}
				Text(
					String.format(Locale.getDefault(), "%.0fh total  ·  %.0fh this week  ·  🔥%dd",
						entry.totalFastingHours, entry.weeklyFastingHours, entry.streakDays),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
			Text(
				"${entry.totalFastingHours.roundToInt()}h",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.primary,
			)
		}
	}
}
