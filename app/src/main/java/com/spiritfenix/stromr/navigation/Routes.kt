package com.spiritfenix.stromr.navigation

object Routes {
    const val EPISODE_LIST = "episode_list"
    const val SONG_LIST = "song_list"
    const val PLAYER = "player/{mediaId}"
    fun player(mediaId: Int) = "player/$mediaId"
}