package com.spiritfenix.stromr.data.local

import androidx.room.Entity

@Entity(
	tableName = "episode_progress",
	primaryKeys = ["guid","feedUrl"]
)
data class EpisodeProgressEntity(
	val guid: String,
	val feedUrl: String,
	val positionMs: Long,
	val updatedAt: Long,
)