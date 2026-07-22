package com.spiritfenix.stromr.ui

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.MediaItem as Media3Item
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
    private val playerListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            _errorMessage.value = friendlyMessage(error)
        }
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
        if (controller != null) {
            startPlayback(item)
        } else {
            pendingItem = item
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
        val media3Item = Media3Item.Builder().setUri(item.audioUrl).setMediaMetadata(metadata).build()
        controller?.apply {
            setMediaItem(media3Item)
            prepare()
            play()
        }
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
        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                R.string.network_error.toString()
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                R.string.network_error.toString()
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                R.string.io_error.toString()
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FAILED ->
                R.string.wrong_format_error.toString()
            else ->
                R.string.default_error.toString()+error.errorCodeName
        }
    }
}