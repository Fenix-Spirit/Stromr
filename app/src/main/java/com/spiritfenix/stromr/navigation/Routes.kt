package com.spiritfenix.stromr.navigation

import android.net.Uri

/**
 * Options->
 * @property EPISODE_LIST
 * @property SONG_LIST
 * @property PLAYER
 * @property SUBSCRIPTIONS
 */
object Routes {
    const val EPISODE_LIST = "episode_list"
    const val SONG_LIST = "song_list"
    const val PLAYER = "player/{mediaId}"
	const val SUBSCRIPTIONS = "subscriptions"
    fun player(mediaId: String) = "player/${Uri.encode(mediaId)}"
}