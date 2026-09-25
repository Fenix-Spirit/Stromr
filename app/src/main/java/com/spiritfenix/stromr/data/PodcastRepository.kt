package com.spiritfenix.stromr.data

import com.spiritfenix.stromr.data.local.AppDatabase
import com.spiritfenix.stromr.data.local.toDomain
import com.spiritfenix.stromr.data.local.toEntity
import com.spiritfenix.stromr.network.rssApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TEST_FEED_URL = "https://changelog.com/podcast/feed"

class PodcastRepository(private val database: AppDatabase) {
    val episodes: Flow<List<MediaItem.Episode>> = database.episodeDao().getAllEpisodes().map { entities -> entities.map { it.toDomain() } }

    suspend fun refresh(){
        val xml = rssApiClient.api.fetchFeed(TEST_FEED_URL).string()
        val episodes = RssParser.parse(xml)
        database.episodeDao().insertAll(episodes.map { it.toEntity() })
    }
}