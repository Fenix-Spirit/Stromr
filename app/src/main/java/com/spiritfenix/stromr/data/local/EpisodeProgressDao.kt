package com.spiritfenix.stromr.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EpisodeProgressDao {
	@Query("SELECT positionMs FROM episode_progress WHERE feedUrl = :feedUrl AND guid = :guid")
	suspend fun getPosition(feedUrl: String, guid: String): Long?
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun save(progress: EpisodeProgressEntity)
}