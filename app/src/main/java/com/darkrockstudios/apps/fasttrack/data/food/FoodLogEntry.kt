package com.darkrockstudios.apps.fasttrack.data.food

import kotlinx.datetime.LocalDateTime

data class FoodLogEntry(
	val id: Int,
	val description: String,
	val time: LocalDateTime,
	val calories: Int? = null,
)
