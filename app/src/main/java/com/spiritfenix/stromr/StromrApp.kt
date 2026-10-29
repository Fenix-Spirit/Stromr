package com.spiritfenix.stromr

import android.app.Application
import com.spiritfenix.stromr.data.SettingsStore
import com.spiritfenix.stromr.network.scheduleFeedRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StromrApp : Application() {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	override fun onCreate() {
		super.onCreate()
		scope.launch {
			val settings = SettingsStore(this@StromrApp)
			scheduleFeedRefresh(
				context = this@StromrApp,
				wifiOnly = settings.wifiOnly.first(),
				refreshInterval = settings.refreshInterval.first())
		}
	}
}