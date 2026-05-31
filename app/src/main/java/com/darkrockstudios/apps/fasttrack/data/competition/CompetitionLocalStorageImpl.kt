package com.darkrockstudios.apps.fasttrack.data.competition

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class CompetitionLocalStorageImpl(context: Context) : CompetitionLocalStorage {
	private val prefs: SharedPreferences =
		context.getSharedPreferences("competition_prefs", Context.MODE_PRIVATE)

	override fun getToken(): String? = prefs.getString("token", null)
	override fun setToken(token: String?) = prefs.edit { putString("token", token) }
	override fun getUserId(): String? = prefs.getString("user_id", null)
	override fun setUserId(id: String?) = prefs.edit { putString("user_id", id) }
	override fun getUsername(): String? = prefs.getString("username", null)
	override fun setUsername(name: String?) = prefs.edit { putString("username", name) }
	override fun getFriendCode(): String? = prefs.getString("friend_code", null)
	override fun setFriendCode(code: String?) = prefs.edit { putString("friend_code", code) }
	override fun getProfileId(): String? = prefs.getString("profile_id", null)
	override fun setProfileId(id: String?) = prefs.edit { putString("profile_id", id) }
	override fun clear() = prefs.edit { clear() }
}
