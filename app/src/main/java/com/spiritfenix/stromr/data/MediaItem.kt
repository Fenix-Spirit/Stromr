package com.spiritfenix.stromr.data
sealed class MediaItem {
    abstract val id: Int
    abstract val title: String
    abstract val audioUrl: String
    abstract val imageUrl: String
    abstract val durationSec:Int

    data class Episode(
        override val id: Int,
        override val title: String,
        override val audioUrl: String,
        override val imageUrl: String,
        override val durationSec:Int,
        val podcastTitle: String,
        val description: String,
        val episodeNumber: Int
    ): MediaItem()
    data class Song(
        override val id: Int,
        override val title: String,
        override val audioUrl: String,
        override val imageUrl: String,
        override val durationSec:Int,
        val artist: String,
        val album: String
    ): MediaItem()
    fun getDurationFormatted(): String {
        try {
            val minutes = durationSec / 60
            val seconds = durationSec % 60
            return "%02d:%02d".format(minutes, seconds)
        } catch (e: Exception) {
            throw e
        }
    }
}