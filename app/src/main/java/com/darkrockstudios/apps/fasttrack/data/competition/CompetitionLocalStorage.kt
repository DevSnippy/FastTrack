package com.darkrockstudios.apps.fasttrack.data.competition

interface CompetitionLocalStorage {
	fun getToken(): String?
	fun setToken(token: String?)
	fun getUserId(): String?
	fun setUserId(id: String?)
	fun getUsername(): String?
	fun setUsername(name: String?)
	fun getFriendCode(): String?
	fun setFriendCode(code: String?)
	fun getProfileId(): String?
	fun setProfileId(id: String?)
	fun clear()
}
