package com.spiritfenix.stromr.data.local

import androidx.room.Entity

@Entity(
	tableName = "episodes",
	primaryKeys = ["guid","feedUrl"]
)
data class EpisodeEntity(
	val guid: String,
	val feedUrl: String,
	val title: String,
	val audioUrl: String,
	val imageUrl: String,
	val durationSec: Int,
	val podcastTitle: String,
	val description: String,
	val episodeNumber: Int
)