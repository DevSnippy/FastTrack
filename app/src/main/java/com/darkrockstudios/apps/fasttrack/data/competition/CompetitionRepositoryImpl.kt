package com.darkrockstudios.apps.fasttrack.data.competition

import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource

class CompetitionRepositoryImpl(
	private val service: PocketBaseService,
	private val local: CompetitionLocalStorage,
	private val settings: SettingsDatasource,
) : CompetitionRepository {

	private fun url() = settings.getPocketBaseUrl()
	private fun token() = local.getToken() ?: error("Not authenticated")

	override fun isAuthenticated(): Boolean = local.getToken() != null
	override fun getStoredUsername(): String? = local.getUsername()
	override fun getStoredFriendCode(): String? = local.getFriendCode()

	override suspend fun login(serverUrl: String, username: String, password: String): Result<Unit> =
		runCatching {
			settings.setPocketBaseUrl(serverUrl)
			val session = service.login(serverUrl, username, password)
			local.setToken(session.token)
			local.setUserId(session.userId)
			local.setUsername(username)
			loadProfile().getOrThrow()
		}

	override suspend fun register(serverUrl: String, username: String, password: String): Result<Unit> =
		runCatching {
			settings.setPocketBaseUrl(serverUrl)
			val session = service.register(serverUrl, username, password)
			local.setToken(session.token)
			local.setUserId(session.userId)
			local.setUsername(username)
			loadProfile().getOrThrow()
		}

	override fun logout() = local.clear()

	// Loads the profile, auto-creating it if it doesn't exist yet.
	override suspend fun loadProfile(): Result<PbProfile> = runCatching {
		val existing = service.getMyProfile(url(), local.getUserId()!!, token())
		val profile = if (existing != null) {
			existing
		} else {
			val code = local.getFriendCode() ?: generateFriendCode()
			service.createProfile(url(), local.getUserId()!!, code, token())
		}
		local.setFriendCode(profile.friendCode)
		local.setProfileId(profile.id)
		profile
	}

	override suspend fun addFriendByCode(code: String): Result<Unit> = runCatching {
		val friendUserId = service.findUserByFriendCode(url(), code, token())
			?: error("No user found with that friend code")
		val myId = local.getUserId()!!
		if (friendUserId == myId) error("That's your own friend code!")
		service.addFriend(url(), myId, friendUserId, token())
	}

	override suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = runCatching {
		service.getLeaderboard(url(), local.getUserId()!!, token())
	}

	override suspend fun syncStats(totalHours: Double, weeklyHours: Double, streakDays: Int): Result<Unit> =
		runCatching {
			val profileId = local.getProfileId() ?: return@runCatching
			service.updateProfile(url(), profileId, totalHours, weeklyHours, streakDays, token())
		}

	private fun generateFriendCode(): String {
		val chars = ('A'..'Z') + ('0'..'9')
		return (1..6).map { chars.random() }.joinToString("")
	}
}
