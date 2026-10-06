package com.spiritfenix.stromr.data.local

import com.spiritfenix.stromr.data.MediaItem

fun EpisodeEntity.toDomain(): MediaItem.Episode = MediaItem.Episode(
	id = "$feedUrl|$guid",
	feedUrl = feedUrl,
	title = title,
	audioUrl = audioUrl,
	imageUrl = imageUrl,
	durationSec = durationSec,
	podcastTitle = podcastTitle,
	description = description,
	episodeNumber = episodeNumber,
	pubDate = pubDate
)

fun MediaItem.Episode.toEntity(): EpisodeEntity = EpisodeEntity(
	guid = id.removePrefix("$feedUrl|"),
	feedUrl = feedUrl,
	title = title,
	audioUrl = audioUrl,
	imageUrl = imageUrl,
	durationSec = durationSec,
	podcastTitle = podcastTitle,
	description = description,
	episodeNumber = episodeNumber,
	pubDate = pubDate
)