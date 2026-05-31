package com.darkrockstudios.apps.fasttrack.data.competition

interface CompetitionRepository {
	fun isAuthenticated(): Boolean
	fun getStoredUsername(): String?
	fun getStoredFriendCode(): String?
	suspend fun login(serverUrl: String, username: String, password: String): Result<Unit>
	suspend fun register(serverUrl: String, username: String, password: String): Result<Unit>
	fun logout()
	suspend fun loadProfile(): Result<PbProfile>
	suspend fun addFriendByCode(code: String): Result<Unit>
	suspend fun getLeaderboard(): Result<List<LeaderboardEntry>>
	suspend fun syncStats(totalHours: Double, weeklyHours: Double, streakDays: Int): Result<Unit>
}
