package com.spiritfenix.stromr.ui

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.service.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.media3.common.MediaItem as Media3Item

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
	private val playerListener = object : Player.Listener {
		override fun onPlayerError(error: PlaybackException) {
			_errorMessage.value = friendlyMessage(error)
		}
		override fun onMediaItemTransition(mediaItem: Media3Item?, reason: Int) {
			_currentMediaId.value = mediaItem?.mediaId
		}
		override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = updateIsPlaying()
		override fun onPlaybackStateChanged(playbackState: Int) = updateIsPlaying()
	}
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
	/**
	 * Starts playback of the given [MediaItem].
	 * @param item the [MediaItem] to play, passed down from the parent (should be [play])
	 * @see MediaItem
	 * @see play
	 */
	private fun startPlayback(item: MediaItem) {
		val metadata = MediaMetadata.Builder()
			.setTitle(item.title)
			.setArtist(
				when (item) {
					is MediaItem.Episode -> item.podcastTitle
					is MediaItem.Song -> item.artist
				}
			)
			.build()
		val media3Item = Media3Item.Builder().setMediaId(item.id).setUri(item.audioUrl).setMediaMetadata(metadata).build()
		controller?.apply {
			setMediaItem(media3Item)
			prepare()
			play()
		}
	}
	private fun updateIsPlaying() {
		val c = controller ?: return
		_isPlaying.value = c.playWhenReady &&
			c.playbackState != Player.STATE_ENDED &&
			c.playbackState != Player.STATE_IDLE
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
}