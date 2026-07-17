package com.spiritfenix.stromr.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.exoplayer.ExoPlayer
import com.spiritfenix.stromr.data.MediaItem

/**
 * ViewModel for the [ExoPlayer].
 * @see MediaItem
 * @see ExoPlayer
 * @see Media3Item
 */
class PlayerViewModel(application: Application): AndroidViewModel(application) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()
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
}