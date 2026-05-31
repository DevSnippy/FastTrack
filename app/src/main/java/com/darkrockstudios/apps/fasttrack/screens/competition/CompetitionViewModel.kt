package com.darkrockstudios.apps.fasttrack.screens.competition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.data.competition.CompetitionRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.DurationUnit

class CompetitionViewModel(
	private val repository: CompetitionRepository,
	private val logRepository: FastingLogRepository,
	private val settings: SettingsDatasource,
) : ViewModel(), ICompetitionViewModel {

	private val _uiState = MutableStateFlow(
		ICompetitionViewModel.CompetitionUiState(
			serverUrl = settings.getPocketBaseUrl(),
		)
	)
	override val uiState: StateFlow<ICompetitionViewModel.CompetitionUiState> = _uiState.asStateFlow()

	override fun init() {
		if (repository.isAuthenticated()) {
			_uiState.update {
				it.copy(
					isAuthenticated = true,
					username = repository.getStoredUsername() ?: "",
					friendCode = repository.getStoredFriendCode() ?: "",
				)
			}
			refreshLeaderboard()
			syncStats()
		}
	}

	override fun onServerUrlChanged(url: String) = _uiState.update { it.copy(serverUrl = url) }
	override fun onUsernameChanged(value: String) = _uiState.update { it.copy(usernameInput = value) }
	override fun onPasswordChanged(value: String) = _uiState.update { it.copy(passwordInput = value) }
	override fun onAddFriendInputChanged(code: String) = _uiState.update { it.copy(addFriendInput = code, addFriendSuccess = false) }
	override fun clearError() = _uiState.update { it.copy(error = null) }

	override fun login() {
		val state = _uiState.value
		if (state.usernameInput.isBlank() || state.passwordInput.isBlank()) return
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			repository.login(state.serverUrl, state.usernameInput, state.passwordInput)
				.onSuccess { onAuthSuccess() }
				.onFailure { _uiState.update { s -> s.copy(isLoading = false, error = it.message) } }
		}
	}

	override fun register() {
		val state = _uiState.value
		if (state.usernameInput.isBlank() || state.passwordInput.isBlank()) return
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			repository.register(state.serverUrl, state.usernameInput, state.passwordInput)
				.onSuccess { onAuthSuccess() }
				.onFailure { _uiState.update { s -> s.copy(isLoading = false, error = it.message) } }
		}
	}

	private suspend fun onAuthSuccess() {
		val profile = repository.loadProfile().getOrNull()
		_uiState.update {
			it.copy(
				isLoading = false,
				isAuthenticated = true,
				username = repository.getStoredUsername() ?: "",
				friendCode = profile?.friendCode ?: "",
			)
		}
		refreshLeaderboard()
		syncStats()
	}

	override fun logout() {
		repository.logout()
		_uiState.update {
			ICompetitionViewModel.CompetitionUiState(serverUrl = settings.getPocketBaseUrl())
		}
	}

	override fun addFriend() {
		val code = _uiState.value.addFriendInput.trim()
		if (code.isBlank()) return
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			repository.addFriendByCode(code)
				.onSuccess {
					_uiState.update { it.copy(isLoading = false, addFriendInput = "", addFriendSuccess = true) }
					refreshLeaderboard()
				}
				.onFailure { _uiState.update { s -> s.copy(isLoading = false, error = it.message) } }
		}
	}

	override fun refreshLeaderboard() {
		viewModelScope.launch {
			repository.getLeaderboard()
				.onSuccess { board -> _uiState.update { it.copy(leaderboard = board) } }
				.onFailure { /* silently ignore leaderboard errors */ }
		}
	}

	private fun syncStats() {
		viewModelScope.launch {
			val entries = logRepository.loadAll().first()
			val totalHours = entries.sumOf { it.length.toDouble(DurationUnit.HOURS) }
			val weekMs = 7L * 24 * 60 * 60 * 1000
			val weekCutoff = System.currentTimeMillis() - weekMs
			val tz = TimeZone.currentSystemDefault()
			val weeklyHours = entries
				.filter { it.start.toInstant(tz).toEpochMilliseconds() >= weekCutoff }
				.sumOf { it.length.toDouble(DurationUnit.HOURS) }
			repository.syncStats(totalHours, weeklyHours, 0)
		}
	}
}
