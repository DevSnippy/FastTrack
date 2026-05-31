package com.darkrockstudios.apps.fasttrack.screens.competition

import com.darkrockstudios.apps.fasttrack.data.competition.LeaderboardEntry
import kotlinx.coroutines.flow.StateFlow

interface ICompetitionViewModel {
	data class CompetitionUiState(
		val isAuthenticated: Boolean = false,
		val isLoading: Boolean = false,
		val error: String? = null,
		// Login/register form
		val serverUrl: String = "http://10.0.2.2:8090",
		val usernameInput: String = "",
		val passwordInput: String = "",
		// Authenticated state
		val username: String = "",
		val friendCode: String = "",
		val addFriendInput: String = "",
		val addFriendSuccess: Boolean = false,
		val leaderboard: List<LeaderboardEntry> = emptyList(),
	)

	val uiState: StateFlow<CompetitionUiState>

	fun init()
	fun onServerUrlChanged(url: String)
	fun onUsernameChanged(value: String)
	fun onPasswordChanged(value: String)
	fun login()
	fun register()
	fun logout()
	fun onAddFriendInputChanged(code: String)
	fun addFriend()
	fun refreshLeaderboard()
	fun clearError()
}
