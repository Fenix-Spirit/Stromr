package com.spiritfenix.stromr.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import coil3.compose.AsyncImage
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.time.Duration.Companion.milliseconds

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
	onItemTap:(String)->Unit = {},
	onAddToQueue: (String)->Unit = {},
	viewModel: MediaViewModel
) {
	val state by viewModel.uiState.collectAsState()
	when (state) {
		is UiState.Loading -> {
			LazyColumn (modifier = modifier.fillMaxSize(),contentPadding = PaddingValues(16.dp),verticalArrangement = Arrangement.spacedBy(8.dp)){
				items (7){
					SkeletonBox(modifier = Modifier.height(100.dp).fillMaxWidth(),cornerRadius = 8.dp)
				}
			}
		}
		is UiState.Error -> {
			Column(
				modifier = modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = stringResource(id = R.string.feed_loading_error),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Spacer(modifier = Modifier.height(12.dp))
				Button(onClick = { viewModel.refresh() }) {
					Text(stringResource(id = R.string.retry))
				}
			}
		}
		is UiState.Success -> {
			val filteredItems = (state as UiState.Success).items.filter(filter)
			LazyColumn(
				modifier = modifier.fillMaxSize(),
				contentPadding = PaddingValues(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			){
				items(filteredItems, key={it.id}) { item ->
					SwipeToQueue(onAddToQueue = { onAddToQueue(item.id) }) {
						CardMedia(item=item,onTap = {onItemTap(item.id)})
					}
				}
			}
		}
	}
}

/**
 * Displays a player screen with information of the given media item
 * @param playerViewModel the view model for the player (passed down from the parent)
 * @param mediaViewModel the view model for the media items (passed down from the parent)
 * @see MediaItem
 * @see PlayerViewModel
 * @see MediaViewModel
 * @see CardMedia
 */
@Composable
fun PlayerScreen(
	playerViewModel: PlayerViewModel,
	mediaViewModel: MediaViewModel
) {
	val mediaId by playerViewModel.currentMediaId.collectAsState()
	RequestNotificationPermissionOnce(trigger = mediaId != null)
	val isPlaying by playerViewModel.isPlaying.collectAsState()
	val uiState by mediaViewModel.uiState.collectAsState()
	val media = (uiState as? UiState.Success)?.items?.find { it.id == mediaId }
	if (media == null) {
		Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Text(stringResource(R.string.nothing_playing))
		}
		return
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
	val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
	val seek: (Long) -> Unit = { playerViewModel.player?.seekTo(it * 1000) }

	if (landscape) {
		Row(
			modifier = Modifier.fillMaxSize().padding(24.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(32.dp)
		) {
			PlayerArtwork(media.imageUrl,Modifier.fillMaxHeight().aspectRatio(1f, matchHeightConstraintsFirst = true))
			PlayerInfo(media.title, position, duration, seek, isPlaying, playerViewModel::togglePlayPause, playerViewModel::skipBack, playerViewModel::skipForward, playerViewModel, Modifier.weight(1f))
		}
	} else {
		Column(
			modifier = Modifier.fillMaxSize().padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.SpaceAround
		) {
			PlayerArtwork(media.imageUrl,Modifier.size(256.dp))
			PlayerInfo(media.title, position, duration, seek, isPlaying, playerViewModel::togglePlayPause,playerViewModel::skipBack, playerViewModel::skipForward, playerViewModel, Modifier.fillMaxWidth())
		}
	}
}
@Composable
private fun RequestNotificationPermissionOnce(trigger: Boolean) {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
	val context = LocalContext.current
	val launcher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) {}
	var asked by rememberSaveable { mutableStateOf(false) }
	LaunchedEffect(trigger) {
		if (trigger && !asked) {
			asked = true
			val granted = ContextCompat.checkSelfPermission(
				context, Manifest.permission.POST_NOTIFICATIONS
			) == PackageManager.PERMISSION_GRANTED
			if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
		}
	}
}
@Composable
private fun PlayerArtwork(img: String, modifier: Modifier = Modifier) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(16.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant),
		contentAlignment = Alignment.Center
	) {
		if (img!="") {
			AsyncImage(
				model = img,
				contentDescription = stringResource(R.string.image_description),
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
			)
		}
		else {
			Image(
				modifier = Modifier.fillMaxSize(),
				painter = painterResource(R.drawable.baseline_broken_image_24),
				contentDescription = stringResource(R.string.broken_image_description),
			)
		}
	}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerInfo(
	title: String,
	position: Long,
	duration: Long,
	onSeek: (Long) -> Unit,
	isPlaying: Boolean,
	onTogglePlayPause: () -> Unit,
	onSkipBack: () -> Unit,
	onSkipForward: () -> Unit,
	playerViewModel: PlayerViewModel? = null,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Playing item $title",
			style = MaterialTheme.typography.headlineMedium,
			textAlign = TextAlign.Center,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis
		)
		if (duration > 0) {
			Text(text = "$position/${duration}s", style = MaterialTheme.typography.bodySmall)
			Slider(
				value = position.toFloat(),
				valueRange = 0f..duration.toFloat(),
				onValueChange = { onSeek(it.toLong()) },
				track = { sliderState ->
					SliderDefaults.Track(
						sliderState = sliderState,
						drawTick = { _, _ -> },
						drawStopIndicator = null,
						colors = SliderDefaults.colors(
							thumbColor = MaterialTheme.colorScheme.primary,
							activeTrackColor = MaterialTheme.colorScheme.primary,
							inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
						)
					)
				}
			)
		} else {
			SkeletonBox(modifier = Modifier.height(14.dp).width(64.dp))
		}
		Box(
			modifier = Modifier.fillMaxWidth(),
			contentAlignment = Alignment.Center
		) {
			Row(
				modifier = Modifier.align(Alignment.Center),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				IconButton(onClick = onSkipBack, modifier = Modifier.size(48.dp)) {
					Icon(
						Icons.Default.Replay10,
						contentDescription = stringResource(R.string.skip_back),
						modifier = Modifier.size(32.dp)
					)
				}
				FilledIconButton(onClick = onTogglePlayPause, modifier = Modifier.size(64.dp)) {
					Icon(
						imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
						contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
						modifier = Modifier.size(36.dp)
					)
				}
				IconButton(onClick = onSkipForward, modifier = Modifier.size(48.dp)) {
					Icon(
						Icons.Default.Forward30,
						contentDescription = stringResource(R.string.skip_forward),
						modifier = Modifier.size(32.dp)
					)
				}
			}
			PlaybackSpeed(
				playerViewModel = playerViewModel,
				modifier = Modifier.align(Alignment.CenterEnd)
			)
		}
	}
}

/**
 * Displays a skeleton box with a pulsing animation
 * @param modifier should contain width and height
 * @param cornerRadius corner radius of the skeleton box, default is 4dp
 */
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

@Composable
fun SubscriptionsScreen(viewModel: MediaViewModel, modifier: Modifier = Modifier) {
	val feeds by viewModel.subscribedFeeds.collectAsState()
	val error by viewModel.subscribeError.collectAsState()
	var input by remember { mutableStateOf("") }

	Column(
		modifier = modifier.fillMaxSize().padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		OutlinedTextField(
			value = input,
			onValueChange = { input = it; viewModel.clearSubscribeError() },
			label = { Text(stringResource(R.string.feed_url)) },
			singleLine = true,
			isError = error != null,
			supportingText = error?.let { { Text(it) } },
			modifier = Modifier.fillMaxWidth()
		)
		Button(
			onClick = { viewModel.subscribe(input) { input = "" } },
			enabled = input.isNotBlank()
		) { Text(stringResource(R.string.subscribe)) }

		LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			items(feeds.toList(), key = { it }) { url ->
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(url, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
					IconButton(onClick = { viewModel.unsubscribe(url) }) {
						Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.unsubscribe))
					}
				}
			}
		}
	}
}
/**
 * Displays a button that triggers a bottom-attached popup to adjust playback speed.
 * The slider changes speed in increments of 0.25x (ranging from 0.5x to 2.0x).
 * Supports both portrait and landscape modes smoothly.
 *
 * @param playerViewModel the view model managing playback speed
 * @param modifier modifier for the trigger button
 */
@Composable
fun PlaybackSpeed(
	playerViewModel: PlayerViewModel? = null,
	modifier: Modifier = Modifier
) {
	var showPopUp by remember { mutableStateOf(false) }
	val vmSpeed by (playerViewModel?.playbackSpeed?.collectAsState() ?: remember { mutableStateOf(null) })
	var localSpeed by remember { mutableFloatStateOf(1.0f) }
	val currentSpeed = vmSpeed ?: localSpeed

	OutlinedButton(
		onClick = { showPopUp = true },
		modifier = modifier,
		contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
	) {
		Text(
			text = formatPlaybackSpeed(currentSpeed),
			style = MaterialTheme.typography.labelLarge
		)
	}

	if (showPopUp) {
		PlaybackSpeedBottomSheet(
			currentSpeed = currentSpeed,
			onSpeedChange = { newSpeed ->
				localSpeed = newSpeed
				playerViewModel?.setPlaybackSpeed(newSpeed)
			},
			onDismiss = { showPopUp = false }
		)
	}
}
/**
 * Bottom-attached sheet containing a slider and quick-select presets for playback speed.
 * Changes speed in increments of 0.25x and handles portrait/landscape orientations cleanly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedBottomSheet(
	currentSpeed: Float,
	onSpeedChange: (Float) -> Unit,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier
) {
	val sheetState = rememberModalBottomSheetState()
	val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		dragHandle = { BottomSheetDefaults.DragHandle() },
		containerColor = MaterialTheme.colorScheme.surface,
		contentColor = MaterialTheme.colorScheme.onSurface,
		modifier = modifier
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
				.padding(bottom = if (isLandscape) 16.dp else 32.dp)
				.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 16.dp)
		) {
			Text(
				text = stringResource(R.string.playback_speed),
				style = MaterialTheme.typography.titleMedium
			)
			Text(
				text = formatPlaybackSpeed(currentSpeed),
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.primary
			)
			Slider(
				value = currentSpeed,
				onValueChange = { raw ->
					val snapped = round(raw / 0.25f) * 0.25f
					onSpeedChange(snapped)
				},
				valueRange = 0.5f..2.0f,
				steps = 5,
				colors = SliderDefaults.colors(
					thumbColor = MaterialTheme.colorScheme.primary,
					activeTrackColor = MaterialTheme.colorScheme.primary,
					inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
					activeTickColor = MaterialTheme.colorScheme.primaryContainer
				),
				modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 1f)
			)
		}
	}
}
private fun formatPlaybackSpeed(speed: Float): String {
	return if (speed % 1f == 0f) {
		"${speed.toInt()}x"
	} else {
		"${speed}x"
	}
}
@Composable
fun SwipeToQueue(
	onAddToQueue: () -> Unit,
	content: @Composable () -> Unit
) {
	val state = rememberSwipeToDismissBoxState()
	val scope = rememberCoroutineScope()
	val currentOnAdd by rememberUpdatedState(onAddToQueue)

	SwipeToDismissBox(
		state = state,
		onDismiss = { value ->
			if (value == SwipeToDismissBoxValue.StartToEnd) currentOnAdd()
			scope.launch { state.reset() }
		},
		enableDismissFromEndToStart = false,
		backgroundContent = {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(MaterialTheme.shapes.medium)
					.background(MaterialTheme.colorScheme.primaryContainer)
					.padding(horizontal = 20.dp),
				contentAlignment = Alignment.CenterStart
			) {
				Icon(
					Icons.AutoMirrored.Filled.PlaylistAdd,
					contentDescription = stringResource(R.string.add_to_queue),
					tint = MaterialTheme.colorScheme.onPrimaryContainer
				)
			}
		}
	) { content() }
}