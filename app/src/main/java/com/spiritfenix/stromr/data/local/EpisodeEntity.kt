package com.spiritfenix.stromr.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val audioUrl: String,
    val imageUrl: String,
    val durationSec: Int,
    val podcastTitle: String,
    val description: String,
    val episodeNumber: Int
)