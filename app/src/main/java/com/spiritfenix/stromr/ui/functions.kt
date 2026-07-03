package com.spiritfenix.stromr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiritfenix.stromr.data.MediaItem

val sampleEpisodes = listOf(
    MediaItem.Episode(1, "Episode 1", "", "", 120,"sla","1d",1),
    MediaItem.Episode(2, "Episode 2", "", "", 135,"sla","2d",2),
    MediaItem.Episode(3, "Episode 3", "", "", 789,"sla","3d",3),
    MediaItem.Episode(4, "Episode 4", "", "", 123,"sla","4d",4),
    MediaItem.Song(5,"Song 1","","",184,"YE","DONDA2"),
    MediaItem.Song(6,"Song 2","","",123,"YE","DONDA2"),
    MediaItem.Song(7,"Song 3","","",123,"YE","DONDA2"),
)
@Composable
fun CardEpisode(episode: MediaItem){
    Card (modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)){
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp))
        {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                val icon = when (episode) {
                    is MediaItem.Episode -> "🎙"
                    is MediaItem.Song   -> "🎵"
                }
                Text(text = icon, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = when(episode){
                        is MediaItem.Episode ->episode.podcastTitle
                        is MediaItem.Song ->episode.album
                    },
                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when(episode){
                        is MediaItem.Episode ->episode.episodeNumber.toString()
                        is MediaItem.Song ->episode.artist
                    },
                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = episode.getDurationFormatted(),
                    style = MaterialTheme.typography.labelSmall,
                    color=MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ListEpisodesScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ){
        items(sampleEpisodes, key={it.id}) { episode ->
            CardEpisode(episode)
        }
    }
}