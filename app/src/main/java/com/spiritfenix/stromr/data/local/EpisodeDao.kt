package com.spiritfenix.stromr.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes ORDER BY pubDate DESC")
    fun getAllEpisodes(): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes")
    suspend fun clearAll()

	@Query("DELETE FROM episodes WHERE feedUrl = :feedUrl")
	suspend fun deleteByFeed(feedUrl: String)
}