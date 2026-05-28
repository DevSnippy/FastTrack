package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FoodEntry(
	@PrimaryKey(autoGenerate = true) val uid: Int = 0,
	@ColumnInfo val description: String,
	@ColumnInfo val timestamp: Long,
	@ColumnInfo val calories: Int? = null
)
