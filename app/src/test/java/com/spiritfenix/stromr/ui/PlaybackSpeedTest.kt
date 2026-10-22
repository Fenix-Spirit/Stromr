package com.spiritfenix.stromr.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.round

class PlaybackSpeedTest {

    @Test
    fun `playback speed snaps to increments of 0_25`() {
        fun snapSpeed(raw: Float): Float = round(raw / 0.25f) * 0.25f

        assertEquals(0.5f, snapSpeed(0.5f), 0.001f)
        assertEquals(0.75f, snapSpeed(0.7f), 0.001f)
        assertEquals(1.0f, snapSpeed(1.02f), 0.001f)
        assertEquals(1.25f, snapSpeed(1.2f), 0.001f)
        assertEquals(1.5f, snapSpeed(1.48f), 0.001f)
        assertEquals(1.75f, snapSpeed(1.8f), 0.001f)
        assertEquals(2.0f, snapSpeed(2.0f), 0.001f)
    }

    @Test
    fun `speed formatting displays clean values`() {
        fun formatPlaybackSpeed(speed: Float): String {
            return if (speed % 1f == 0f) {
                "${speed.toInt()}x"
            } else {
                "${speed}x"
            }
        }

        assertEquals("0.5x", formatPlaybackSpeed(0.5f))
        assertEquals("0.75x", formatPlaybackSpeed(0.75f))
        assertEquals("1x", formatPlaybackSpeed(1.0f))
        assertEquals("1.25x", formatPlaybackSpeed(1.25f))
        assertEquals("1.5x", formatPlaybackSpeed(1.5f))
        assertEquals("2x", formatPlaybackSpeed(2.0f))
    }
}
