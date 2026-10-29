package com.spiritfenix.stromr.network

import android.content.Context
import androidx.work.*
import com.spiritfenix.stromr.data.PodcastRepository
import com.spiritfenix.stromr.data.SubscriptionStore
import com.spiritfenix.stromr.data.local.AppDatabase
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.util.concurrent.TimeUnit

class FeedRefreshWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {
	override suspend fun doWork(): Result {
		val repo = PodcastRepository(
			AppDatabase.getInstance(applicationContext),
			SubscriptionStore(applicationContext)
		)
		return try {
			repo.refresh()
			Result.success()
		} catch (e: CancellationException) {
			throw e
		} catch (_: IOException) {
			Result.retry()
		} catch (_: Exception) {
			Result.failure()
		}
	}
}
fun scheduleFeedRefresh(context: Context, wifiOnly:Boolean, refreshInterval:Int){
	val request = PeriodicWorkRequestBuilder<FeedRefreshWorker>(refreshInterval.toLong(), TimeUnit.HOURS)
		.setConstraints(
			Constraints.Builder()
				.setRequiredNetworkType(if(wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
				.build()
		)
		.build()
	WorkManager.getInstance(context).enqueueUniquePeriodicWork(
		"feed_refresh",
		ExistingPeriodicWorkPolicy.UPDATE,
		request
	)
}