package com.spiritfenix.stromr.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.PlaybackException
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.spiritfenix.stromr.R.string

/**
 * ViewModel for the [ExoPlayer].
 * @see MediaItem
 * @see ExoPlayer
 * @see Media3Item
 */
class PlayerViewModel(application: Application): AndroidViewModel(application) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                _errorMessage.value = friendlyMessage(error)
            }
        })
    }
    fun play(item: MediaItem){
        val media3Item = Media3Item.fromUri(item.audioUrl)
        exoPlayer.setMediaItem(media3Item)
        exoPlayer.prepare()
        exoPlayer.play()
    }
    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
    fun clearError(){
        _errorMessage.value=null
    }
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