package com.spiritfenix.stromr.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.spiritfenix.stromr.network.scheduleFeedRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsStore(private val context: Context) {
	private val store = context.applicationContext.dataStore
	private val wifiOnlyKey = booleanPreferencesKey("wifiOnly")
	private val refreshIntervalKey = intPreferencesKey("refreshInterval")

	val wifiOnly: Flow<Boolean> = store.data.map { preferences ->
		preferences[wifiOnlyKey] ?: false
	}

	val refreshInterval: Flow<Int> = store.data.map { preferences ->
		preferences[refreshIntervalKey] ?: 6
	}
	suspend fun setRefreshInterval(interval: Int) {
		store.edit { preferences ->
			preferences[refreshIntervalKey] = interval
		}
		scheduleFeedRefresh(context, wifiOnly.first(), interval)
	}
	suspend fun setWifiOnly(wifiOnly: Boolean) {
		store.edit { preferences ->
			preferences[wifiOnlyKey] = wifiOnly
		}
		scheduleFeedRefresh(context, wifiOnly, refreshInterval.first())
	}
}