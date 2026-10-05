package com.spiritfenix.stromr.data

import com.spiritfenix.stromr.data.local.AppDatabase
import com.spiritfenix.stromr.data.local.toDomain
import com.spiritfenix.stromr.data.local.toEntity
import com.spiritfenix.stromr.network.rssApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class PodcastRepository(
	private val database: AppDatabase,
	private val subscriptions: SubscriptionStore
) {
	private val dao = database.episodeDao()
	val episodes: Flow<List<MediaItem.Episode>> = dao.getAllEpisodes().map { entities -> entities.map { it.toDomain() } }
	val subscribedFeeds: Flow<Set<String>> = subscriptions.feedUrls

	suspend fun refresh() {
		val urls = subscriptions.feedUrls.first()
		var anySucceeded = false
		var lastError: Exception? = null
		for (url in urls) {
			try {
				refreshFeed(url)
				anySucceeded = true
			} catch (e: CancellationException) {
				throw e
			} catch (e: Exception) {
				lastError = e
			}
		}
		if (!anySucceeded) lastError?.let { throw it }
	}
	private suspend fun refreshFeed(feedUrl: String) {
		val xml = rssApiClient.api.fetchFeed(feedUrl).string()
		val parsed = RssParser.parse(xml, feedUrl)
		if (feedUrl !in subscriptions.feedUrls.first()) return
		dao.insertAll(parsed.map { it.toEntity() })
	}
	suspend fun subscribe(feedUrl: String) {
		require(feedUrl.startsWith("http://") || feedUrl.startsWith("https://"))
		val xml = rssApiClient.api.fetchFeed(feedUrl).string()
		val parsed = RssParser.parse(xml, feedUrl)
		require(parsed.isNotEmpty())
		subscriptions.subscribe(feedUrl)
		dao.insertAll(parsed.map { it.toEntity() })
	}
	suspend fun unsubscribe(feedUrl: String) {
		dao.deleteByFeed(feedUrl)
		subscriptions.unsubscribe(feedUrl)
	}
}