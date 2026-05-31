package com.darkrockstudios.apps.fasttrack.data.competition

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PbAuthSession(val token: String, val userId: String)
data class PbProfile(
	val id: String,
	val userId: String,
	val friendCode: String,
	val totalFastingHours: Double,
	val weeklyFastingHours: Double,
	val streakDays: Int,
	val username: String,
)
data class LeaderboardEntry(
	val username: String,
	val totalFastingHours: Double,
	val weeklyFastingHours: Double,
	val streakDays: Int,
	val isMe: Boolean,
)

class PocketBaseService {

	// ─── HTTP helpers ────────────────────────────────────────────────────────

	private suspend fun get(baseUrl: String, path: String, token: String? = null): JSONObject =
		withContext(Dispatchers.IO) {
			val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
				requestMethod = "GET"
				token?.let { setRequestProperty("Authorization", it) }
				connectTimeout = 10_000
				readTimeout = 10_000
			}
			conn.readResponse()
		}

	private suspend fun post(baseUrl: String, path: String, body: JSONObject, token: String? = null): JSONObject =
		withContext(Dispatchers.IO) {
			val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
				requestMethod = "POST"
				setRequestProperty("Content-Type", "application/json")
				token?.let { setRequestProperty("Authorization", it) }
				doOutput = true
				connectTimeout = 10_000
				readTimeout = 10_000
			}
			OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
			conn.readResponse()
		}

	private suspend fun patch(baseUrl: String, path: String, body: JSONObject, token: String): JSONObject =
		withContext(Dispatchers.IO) {
			val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
				requestMethod = "PATCH"
				setRequestProperty("Content-Type", "application/json")
				setRequestProperty("Authorization", token)
				doOutput = true
				connectTimeout = 10_000
				readTimeout = 10_000
			}
			OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
			conn.readResponse()
		}

	private fun HttpURLConnection.readResponse(): JSONObject {
		val code = responseCode
		val text = (if (code in 200..299) inputStream else errorStream)
			.bufferedReader().readText()
		disconnect()
		if (code !in 200..299) throw Exception(
			JSONObject(text).optString("message", "HTTP $code")
		)
		return JSONObject(text)
	}

	// ─── Auth ─────────────────────────────────────────────────────────────────

	suspend fun login(baseUrl: String, username: String, password: String): PbAuthSession {
		val body = JSONObject().apply {
			put("identity", username)
			put("password", password)
		}
		val resp = post(baseUrl, "/api/collections/users/auth-with-password", body)
		return PbAuthSession(
			token = resp.getString("token"),
			userId = resp.getJSONObject("record").getString("id"),
		)
	}

	suspend fun register(baseUrl: String, username: String, password: String): PbAuthSession {
		val body = JSONObject().apply {
			put("username", username)
			put("password", password)
			put("passwordConfirm", password)
		}
		post(baseUrl, "/api/collections/users/records", body)
		return login(baseUrl, username, password)
	}

	// ─── Profiles ─────────────────────────────────────────────────────────────

	suspend fun createProfile(baseUrl: String, userId: String, friendCode: String, token: String): PbProfile {
		val body = JSONObject().apply {
			put("userId", userId)
			put("friend_code", friendCode)
			put("total_fasting_hours", 0)
			put("weekly_fasting_hours", 0)
			put("streak_days", 0)
		}
		val resp = post(baseUrl, "/api/collections/profiles/records", body, token)
		return resp.toProfile(username = "")
	}

	suspend fun getMyProfile(baseUrl: String, userId: String, token: String): PbProfile? {
		val filter = URLEncoder.encode("userId=\"$userId\"", "UTF-8")
		val resp = get(baseUrl, "/api/collections/profiles/records?filter=$filter&expand=userId", token)
		val items = resp.getJSONArray("items")
		if (items.length() == 0) return null
		val item = items.getJSONObject(0)
		val username = item.optJSONObject("expand")
			?.optJSONObject("userId")
			?.optString("username", "") ?: ""
		return item.toProfile(username)
	}

	suspend fun updateProfile(
		baseUrl: String,
		profileId: String,
		totalFastingHours: Double,
		weeklyFastingHours: Double,
		streakDays: Int,
		token: String,
	) {
		val body = JSONObject().apply {
			put("total_fasting_hours", totalFastingHours)
			put("weekly_fasting_hours", weeklyFastingHours)
			put("streak_days", streakDays)
		}
		patch(baseUrl, "/api/collections/profiles/records/$profileId", body, token)
	}

	// ─── Friends ──────────────────────────────────────────────────────────────

	suspend fun findUserByFriendCode(baseUrl: String, code: String, token: String): String? {
		val filter = URLEncoder.encode("friend_code=\"${code.uppercase()}\"", "UTF-8")
		val resp = get(baseUrl, "/api/collections/profiles/records?filter=$filter", token)
		val items = resp.getJSONArray("items")
		if (items.length() == 0) return null
		return items.getJSONObject(0).getString("userId")
	}

	suspend fun addFriend(baseUrl: String, myUserId: String, friendUserId: String, token: String) {
		val body = JSONObject().apply {
			put("userId", myUserId)
			put("friendId", friendUserId)
		}
		post(baseUrl, "/api/collections/friendships/records", body, token)
	}

	suspend fun getLeaderboard(baseUrl: String, myUserId: String, token: String): List<LeaderboardEntry> {
		// Get my friendships
		val filter = URLEncoder.encode("userId=\"$myUserId\"", "UTF-8")
		val friendsResp = get(baseUrl, "/api/collections/friendships/records?filter=$filter&perPage=50", token)
		val friendIds = (0 until friendsResp.getJSONArray("items").length())
			.map { friendsResp.getJSONArray("items").getJSONObject(it).getString("friendId") }

		// Get profiles: mine + friends
		val allIds = (listOf(myUserId) + friendIds).toSet()
		if (allIds.isEmpty()) return emptyList()

		val profileFilter = URLEncoder.encode(
			allIds.joinToString(" || ") { "userId=\"$it\"" }, "UTF-8"
		)
		val profilesResp = get(
			baseUrl,
			"/api/collections/profiles/records?filter=$profileFilter&expand=userId&perPage=50",
			token,
		)
		val items = profilesResp.getJSONArray("items")

		return (0 until items.length()).map { i ->
			val item = items.getJSONObject(i)
			val username = item.optJSONObject("expand")
				?.optJSONObject("userId")
				?.optString("username", "?") ?: "?"
			LeaderboardEntry(
				username = username,
				totalFastingHours = item.optDouble("total_fasting_hours", 0.0),
				weeklyFastingHours = item.optDouble("weekly_fasting_hours", 0.0),
				streakDays = item.optInt("streak_days", 0),
				isMe = item.getString("userId") == myUserId,
			)
		}.sortedByDescending { it.totalFastingHours }
	}

	// ─── JSON helpers ─────────────────────────────────────────────────────────

	private fun JSONObject.toProfile(username: String) = PbProfile(
		id = getString("id"),
		userId = getString("userId"),
		friendCode = optString("friend_code", ""),
		totalFastingHours = optDouble("total_fasting_hours", 0.0),
		weeklyFastingHours = optDouble("weekly_fasting_hours", 0.0),
		streakDays = optInt("streak_days", 0),
		username = username,
	)
}
