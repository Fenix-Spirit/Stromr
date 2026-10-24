package com.spiritfenix.stromr.ui

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
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
/**
 * ViewModel that controls playback via a [MediaController] connected to [PlaybackService].
 * @see MediaController
 * @see PlaybackService
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
	private var controller: MediaController? = null
	private var controllerFuture: ListenableFuture<MediaController>? = null
	private var pendingItem: MediaItem? = null
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
	private val playerListener = object : Player.Listener {
		override fun onPlayerError(error: PlaybackException) {
			_errorMessage.value = friendlyMessage(error)
		}
		override fun onMediaItemTransition(mediaItem: Media3Item?, reason: Int) {
			_currentMediaId.value = mediaItem?.mediaId
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
				pendingItem?.let { item ->
					startPlayback(item)
					pendingItem = null
				}
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
	fun play(item: MediaItem) {
		val c = controller
		if (c == null) {
			_currentMediaId.value = item.id
			pendingItem = item
			return
		}
		val sameItem = c.currentMediaItem?.mediaId == item.id
		val active = c.playbackState == Player.STATE_READY || c.playbackState == Player.STATE_BUFFERING
		if (sameItem && active) return
		startPlayback(item)
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
	private fun startPlayback(item: MediaItem) {
		saveProgress()
		val media3Item = item.toMedia3Item()
		viewModelScope.launch {
			val start = (item as? MediaItem.Episode)?.let {
				progressDao.getPosition(it.feedUrl, it.id.removePrefix("${it.feedUrl}|"))
			} ?: 0L
			controller?.apply {
				setMediaItem(media3Item, start)
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
		val extras = c.currentMediaItem?.mediaMetadata?.extras ?: return
		val feedUrl = extras.getString(KEY_FEED_URL) ?: return
		val guid = extras.getString(KEY_GUID) ?: return
		if (c.playbackState != Player.STATE_READY && c.playbackState != Player.STATE_ENDED) return
		val duration = c.duration
		val raw = c.currentPosition
		val pos = if (duration != C.TIME_UNSET && raw >= duration - 10_000) 0L else raw
		val row = EpisodeProgressEntity(feedUrl = feedUrl, guid = guid, positionMs = pos, updatedAt = System.currentTimeMillis())
		viewModelScope.launch { progressDao.save(row) }
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
	private fun MediaItem.toMedia3Item(): Media3Item {
		val extras = Bundle()
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
}