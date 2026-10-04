package com.spiritfenix.stromr.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SubscriptionStore(context: Context) {
	private val store = context.applicationContext.dataStore
	private val key = stringSetPreferencesKey("subscribed_feeds")

	val feedUrls: Flow<Set<String>> = store.data.map { it[key] ?: DEFAULT_FEEDS }

	suspend fun subscribe(url: String) = store.edit { prefs ->
		prefs[key] = (prefs[key] ?: DEFAULT_FEEDS) + url
	}

	suspend fun unsubscribe(url: String) = store.edit { prefs ->
		prefs[key] = (prefs[key] ?: DEFAULT_FEEDS) - url
	}

	companion object {
		val DEFAULT_FEEDS = setOf("https://changelog.com/podcast/feed")
	}
}