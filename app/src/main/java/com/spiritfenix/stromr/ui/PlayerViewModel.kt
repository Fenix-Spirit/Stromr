package com.spiritfenix.stromr.ui

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.data.UpNextEntry
import com.spiritfenix.stromr.data.local.AppDatabase
import com.spiritfenix.stromr.data.local.EpisodeProgressEntity
import com.spiritfenix.stromr.service.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import androidx.media3.common.MediaItem as Media3Item

private const val KEY_FEED_URL = "feedUrl"
private const val KEY_GUID = "guid"
private const val KEY_QUEUED = "queued"
/**
 * ViewModel that controls playback via a [MediaController] connected to [PlaybackService].
 * @see MediaController
 * @see PlaybackService
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
	private var controller: MediaController? = null
	private var controllerFuture: ListenableFuture<MediaController>? = null
	private var pendingItems: List<MediaItem>? = null
	val player: Player?
		get() = controller
	private val _errorMessage = MutableStateFlow<String?>(null)
	val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
	private val _currentMediaId = MutableStateFlow<String?>(null)
	val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()
	private val _isPlaying = MutableStateFlow(false)
	val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
	private val _playbackSpeed = MutableStateFlow(1f)
	val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
	private val _upNext = MutableStateFlow<List<UpNextEntry>>(emptyList())
	val upNext: StateFlow<List<UpNextEntry>> = _upNext.asStateFlow()
	private val _hasNext = MutableStateFlow(false)
	val hasNext: StateFlow<Boolean> = _hasNext.asStateFlow()
	private val _hasPrevious = MutableStateFlow(false)
	val hasPrevious: StateFlow<Boolean> = _hasPrevious.asStateFlow()
	private val playerListener = object : Player.Listener {
		override fun onPlayerError(error: PlaybackException) {
			_errorMessage.value = friendlyMessage(error)
		}
		override fun onMediaItemTransition(mediaItem: Media3Item?, reason: Int) {
			_currentMediaId.value = mediaItem?.mediaId
			if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
				reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
				restorePosition(mediaItem)
			}
			refreshQueue()
		}
		override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = updateIsPlaying()
		override fun onPlaybackStateChanged(playbackState: Int) = updateIsPlaying()
		override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
			_playbackSpeed.value = playbackParameters.speed
		}
		override fun onIsPlayingChanged(isPlaying: Boolean) {
			saveJob?.cancel()
			if (isPlaying) {
				saveJob = viewModelScope.launch {
					while (true) {
						delay(10_000.milliseconds)
						saveProgress()
					}
				}
			} else {
				saveProgress()
			}
		}
		@OptIn(UnstableApi::class)
		override fun onPositionDiscontinuity(
			oldPosition: Player.PositionInfo,
			newPosition: Player.PositionInfo,
			reason: Int
		) {
			when (reason) {
				Player.DISCONTINUITY_REASON_AUTO_TRANSITION ->
					saveFor(oldPosition.mediaItem, 0L, C.TIME_UNSET)
				Player.DISCONTINUITY_REASON_SEEK ->
					if (oldPosition.mediaItem?.mediaId != newPosition.mediaItem?.mediaId)
						saveFor(oldPosition.mediaItem, oldPosition.positionMs, C.TIME_UNSET)
			}
		}
		override fun onTimelineChanged(timeline: Timeline, reason: Int) = refreshQueue()
	}
	private val progressDao = AppDatabase.getInstance(application).ProgressDao()
	private var saveJob: Job? = null
	init {
		val sessionToken = SessionToken(
			application,
			ComponentName(application, PlaybackService::class.java)
		)
		controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
		controllerFuture?.addListener(
			{
				controller = controllerFuture?.get()
				controller?.addListener(playerListener)
				_currentMediaId.value = controller?.currentMediaItem?.mediaId
				_playbackSpeed.value = controller?.playbackParameters?.speed ?: 1f
				updateIsPlaying()
				pendingItems?.let { items ->
					startPlayback(items)
					pendingItems = null
				}
				refreshQueue()
			},
			ContextCompat.getMainExecutor(application)
		)
	}

	/**
	 * Plays the given [MediaItem].
	 * @param item the [MediaItem] to play
	 * @see MediaItem
	 * @see startPlayback
	 */
	fun play(items: List<MediaItem>) {
		val first = items.firstOrNull() ?: return
		val c = controller
		if (c == null) {
			_currentMediaId.value = first.id
			pendingItems = items
			return
		}
		val sameItem = c.currentMediaItem?.mediaId == first.id
		val active = c.playbackState == Player.STATE_READY || c.playbackState == Player.STATE_BUFFERING
		if (sameItem && active) return
		startPlayback(items)
	}
	fun togglePlayPause() {
		val c = controller ?: return
		if (_isPlaying.value) {
			c.pause()
		} else {
			when (c.playbackState) {
				Player.STATE_ENDED -> c.seekToDefaultPosition()
				Player.STATE_IDLE -> c.prepare()
			}
			c.play()
		}
	}
	fun skipBack() { controller?.seekBack() }
	fun skipForward() { controller?.seekForward() }
	/**
	 * Starts playback of the given [MediaItem].
	 * @param item the [MediaItem] to play, passed down from the parent (should be [play])
	 * @see MediaItem
	 * @see play
	 */
	private fun startPlayback(items: List<MediaItem>) {
		saveProgress()
		val first = items.first()
		val media3Items = items.map { it.toMedia3Item(false) }
		viewModelScope.launch {
			val start = (first as? MediaItem.Episode)?.let {
				progressDao.getPosition(it.feedUrl, it.id.removePrefix("${it.feedUrl}|"))
			} ?: 0L
			controller?.apply {
				setMediaItems(media3Items, 0,start)
				prepare()
				play()
			}
		}
	}
	private fun updateIsPlaying() {
		val c = controller ?: return
		_isPlaying.value = c.playWhenReady &&
			c.playbackState != Player.STATE_ENDED &&
			c.playbackState != Player.STATE_IDLE
	}
	private fun saveProgress() {
		val c = controller ?: return
		if (c.playbackState != Player.STATE_READY && c.playbackState != Player.STATE_ENDED) return
		saveFor(c.currentMediaItem, c.currentPosition, c.duration)
	}
	private fun saveFor(item: Media3Item?, positionMs: Long, durationMs: Long) {
		val extras = item?.mediaMetadata?.extras ?: return
		val feedUrl = extras.getString(KEY_FEED_URL) ?: return
		val guid = extras.getString(KEY_GUID) ?: return
		val pos = if (durationMs != C.TIME_UNSET && positionMs >= durationMs - 10_000) 0L else positionMs
		val row = EpisodeProgressEntity(guid = guid, feedUrl = feedUrl, positionMs = pos, updatedAt = System.currentTimeMillis())
		viewModelScope.launch { progressDao.save(row) }
	}
	private fun restorePosition(item: Media3Item?) {
		val extras = item?.mediaMetadata?.extras ?: return
		val feedUrl = extras.getString(KEY_FEED_URL) ?: return
		val guid = extras.getString(KEY_GUID) ?: return
		viewModelScope.launch {
			val pos = progressDao.getPosition(feedUrl, guid) ?: return@launch
			if (controller?.currentMediaItem?.mediaId == item.mediaId) controller?.seekTo(pos)
		}
	}
	fun setPlaybackSpeed(speed: Float){
		_playbackSpeed.value = speed
		controller?.setPlaybackSpeed(speed)
	}
	override fun onCleared() {
		controller?.removeListener(playerListener)
		controllerFuture?.let { MediaController.releaseFuture(it) }
		super.onCleared()
	}
	fun clearError(){
		_errorMessage.value=null
	}

	/**
	 * Returns a user-friendly error message from a [PlaybackException].
	 */
	private fun friendlyMessage(error: PlaybackException): String {
		val context = getApplication<Application>()
		return when (error.errorCode) {
			PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
			PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
				context.getString(R.string.network_error)
			PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
				context.getString(R.string.server_error)
			PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
				context.getString(R.string.io_error)
			PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
			PlaybackException.ERROR_CODE_DECODING_FAILED ->
				context.getString(R.string.wrong_format_error)
			else ->
				context.getString(R.string.default_error)+error.errorCodeName
		}
	}
	private fun MediaItem.toMedia3Item(queued: Boolean): Media3Item {
		val extras = Bundle()
		if (queued) {
			extras.putBoolean(KEY_QUEUED, true)
		}
		if (this is MediaItem.Episode) {
			extras.putString(KEY_FEED_URL, feedUrl)
			extras.putString(KEY_GUID, id.removePrefix("$feedUrl|"))
		}
		val metadata = MediaMetadata.Builder()
			.setTitle(title)
			.setArtist(
				when (this) {
					is MediaItem.Episode -> podcastTitle
					is MediaItem.Song -> artist
				}
			)
			.setExtras(extras)
			.build()
		return Media3Item.Builder()
			.setMediaId(id)
			.setUri(audioUrl)
			.setMediaMetadata(metadata)
			.build()
	}
	fun addToQueue(item: MediaItem) {
		val c = controller ?: return
		if (c.mediaItemCount == 0) {
			play(listOf(item))
			return
		}
		var index = c.currentMediaItemIndex + 1
		while (index < c.mediaItemCount &&
			c.getMediaItemAt(index).mediaMetadata.extras?.getBoolean(KEY_QUEUED) == true
		) index++
		c.addMediaItem(index, item.toMedia3Item(queued = true))
		_errorMessage.value = getApplication<Application>().getString(R.string.added_to_queue)
	}
	private fun refreshQueue() {
		val c = controller ?: return
		_hasNext.value = c.hasNextMediaItem()
		_hasPrevious.value = c.hasPreviousMediaItem()
		_upNext.value = ((c.currentMediaItemIndex + 1) until c.mediaItemCount).map { i ->
			val item = c.getMediaItemAt(i)
			UpNextEntry(
				index = i,
				title = item.mediaMetadata.title?.toString().orEmpty(),
				subtitle = item.mediaMetadata.artist?.toString().orEmpty(),
				queued = item.mediaMetadata.extras?.getBoolean(KEY_QUEUED) == true
			)
		}
	}
	fun nextItem() { controller?.seekToNextMediaItem() }
	fun previousItem() { controller?.seekToPreviousMediaItem() }
	fun jumpTo(index: Int) { controller?.seekTo(index, 0L) }
}