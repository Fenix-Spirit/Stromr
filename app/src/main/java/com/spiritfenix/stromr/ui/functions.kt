package com.spiritfenix.stromr.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

val sampleEpisodes = listOf(
    MediaItem.Episode(1, "Episode 1", "https://www.myinstants.com/media/sounds/mbappe-dictador_bAAKC8q.mp3", "", 120,"sla","1d",1),
    MediaItem.Episode(2, "Episode 2", "https://www.myinstants.com/media/sounds/mbappe-dictador_bAAKC8q.mp3", "", 135,"sla","2d",2),
    MediaItem.Episode(3, "Episode 3", "https://www.myinstants.com/media/sounds/du-bist-gut-genug.mp3", "", 789,"sla","3d",3),
    MediaItem.Episode(4, "Episode 4", "https://www.myinstants.com/media/sounds/du-bist-gut-genug.mp3", "", 123,"sla","4d",4),
    MediaItem.Song(5,"Song 1","https://www.myinstants.com/media/sounds/mbappe-dictador_bAAKC8q.mp3","",184,"YE","DONDA2"),
    MediaItem.Song(6,"Song 2","https://www.myinstants.com/media/sounds/mbappe-dictador_bAAKC8q.mp3","",123,"YE","DONDA2"),
    MediaItem.Song(7,"Song 3","https://www.myinstants.com/media/sounds/du-bist-gut-genug.mp3","",123,"YE","DONDA2"),
)

/**
 * Creates a card for a media item
 * @param item the media item to display
 * @param onTap a function to be called when the card is tapped
 * @see MediaItem
 * @see ListMediaScreen
 */
@Composable
fun CardMedia(item: MediaItem, onTap:()->Unit = {}){
    Card (modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onTap){
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp))
        {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                val icon = when (item) {
                    is MediaItem.Episode -> "🎙"
                    is MediaItem.Song   -> "🎵"
                }
                Text(text = icon, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = when(item){
                        is MediaItem.Episode ->item.podcastTitle
                        is MediaItem.Song ->item.album
                    },
                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when(item){
                        is MediaItem.Episode ->item.episodeNumber.toString()
                        is MediaItem.Song ->item.artist
                    },
                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.getDurationFormatted(),
                    style = MaterialTheme.typography.labelSmall,
                    color=MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
/**
 * Displays a LazyColumn with media items
 * @param modifier the modifier for the list
 * @param filter a function to filter the items
 * @param onItemTap a function to be called when an item is tapped, gets passed down to CardMedia
 * @param viewModel the view model for the list
 * @see MediaItem
 * @see CardMedia
 */
@Composable
fun ListMediaScreen(
    modifier: Modifier = Modifier,
    filter: (MediaItem)-> Boolean,
    onItemTap:(Int)->Unit = {},
    viewModel: MediaViewModel
) {
    val allItems by viewModel.items.collectAsState()
    val filteredItems = allItems.filter(filter)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ){
        items(filteredItems, key={it.id}) { item ->
            CardMedia(item=item,onTap = {onItemTap(item.id)})
        }
    }
}

/**
 * Displays a player screen with information of the given media item
 * @param mediaId the id of the media item to play
 * @param playerViewModel the view model for the player
 * @param mediaViewModel the view model for the media items
 * @see MediaItem
 * @see PlayerViewModel
 * @see MediaViewModel
 * @see CardMedia
 */
@Composable
fun PlayerScreen(
    mediaId: Int,
    playerViewModel: PlayerViewModel,
    mediaViewModel: MediaViewModel
) {
    val media= mediaViewModel.FindById(mediaId)?:return
    LaunchedEffect(media) {
        playerViewModel.play(media)
    }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    LaunchedEffect(playerViewModel.player) {
        while (true) {
            position = (playerViewModel.player?.currentPosition.takeIf { it != C.TIME_UNSET } ?: 0L )/ 1000
            duration = (playerViewModel.player?.duration.takeIf { it!= C.TIME_UNSET } ?: 0L)/ 1000
            delay(500.milliseconds)
        }
    }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
        Box(modifier = Modifier.size(256.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.baseline_broken_image_24),
                contentDescription = stringResource(R.string.default_image_description),
            )
        }
        Text(
            text = "Playing item ${media.title}",
            style = MaterialTheme.typography.headlineMedium
        )
        if (duration > 0) {
            Text(
                text="$position/${duration}s",
                style=MaterialTheme.typography.bodySmall
            )
        }
        else{
            SkeletonBox(modifier = Modifier.height(14.dp).width(64.dp))
        }
    }
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier,cornerRadius: Dp = 4.dp) {
    val transition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)),
    )
}