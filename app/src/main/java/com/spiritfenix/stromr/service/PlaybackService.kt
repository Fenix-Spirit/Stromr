package com.spiritfenix.stromr.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.spiritfenix.stromr.MainActivity

/**
 * Service for playback.
 * 1. Creates a MediaSession
 * 2. Creates an ExoPlayer
 * 3. ExoPlayer is attached to the MediaSession
 * @see MediaSessionService
 * @see ExoPlayer
 * @see MediaSession
 */
class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

	@OptIn(UnstableApi::class)
	override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
			.setAudioAttributes(
				AudioAttributes.Builder()
					.setUsage(C.USAGE_MEDIA)
					.setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
					.build(),
				true
			)
			.setHandleAudioBecomingNoisy(true)
			.setSeekBackIncrementMs(10_000)
			.setSeekForwardIncrementMs(30_000)
			.build()
		val openPlayer = Intent(this, MainActivity::class.java).apply {
			action = MainActivity.ACTION_OPEN_PLAYER
			flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		val sessionActivity = PendingIntent.getActivity(
			this, 0, openPlayer, PendingIntent.FLAG_IMMUTABLE
		)
		mediaSession = MediaSession.Builder(this, player)
			.setSessionActivity(sessionActivity)
			.build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

	@OptIn(UnstableApi::class)
	override fun onTaskRemoved(rootIntent: Intent?) {
		pauseAllPlayersAndStopSelf()
	}
}