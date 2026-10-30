package com.spiritfenix.stromr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.data.SettingsStore
import com.spiritfenix.stromr.navigation.NavGraph
import com.spiritfenix.stromr.navigation.Routes
import com.spiritfenix.stromr.ui.MediaViewModel
import com.spiritfenix.stromr.ui.MiniPlayer
import com.spiritfenix.stromr.ui.PlayerViewModel
import com.spiritfenix.stromr.ui.UiState
import com.spiritfenix.stromr.ui.theme.StrömrTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
	private var openPlayerRequest by mutableIntStateOf(0)
	private fun handleIntent(intent: Intent?) {
		if (intent?.action == ACTION_OPEN_PLAYER) openPlayerRequest++
	}
	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		handleIntent(intent)
	}
	companion object {
		const val ACTION_OPEN_PLAYER = "com.spiritfenix.stromr.OPEN_PLAYER"
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) handleIntent(intent)
		enableEdgeToEdge()
		setContent {
			StrömrTheme {
				val navController = rememberNavController()
				val settings = SettingsStore(this)
				LaunchedEffect(openPlayerRequest) {
					if (openPlayerRequest > 0) {
						navController.navigate(Routes.PLAYER) {
							launchSingleTop = true
							popUpTo(Routes.PLAYER)
						}
					}
				}
				val navBackStackEntry by navController.currentBackStackEntryAsState()
				val currentRoute = navBackStackEntry?.destination?.route
				val mediaViewModel: MediaViewModel = viewModel()
				val playerViewModel: PlayerViewModel = viewModel()
				val snackbarHostState = remember { SnackbarHostState() }
				val errorMessagePlay by playerViewModel.errorMessage.collectAsState()
				val messageLoad by mediaViewModel.uiState.collectAsState()
				val currentMediaId by playerViewModel.currentMediaId.collectAsState()
				val isPlaying by playerViewModel.isPlaying.collectAsState()
				val nowPlaying = remember(currentMediaId, messageLoad) {
					(messageLoad as? UiState.Success)?.items?.find { it.id == currentMediaId }
				}
				var lastPlaying by remember { mutableStateOf<MediaItem?>(null) }
				if (nowPlaying != null) lastPlaying = nowPlaying
				val showMini = nowPlaying != null && currentRoute != Routes.PLAYER
				var progress by remember { mutableFloatStateOf(0f) }
				LaunchedEffect(showMini) {
					while (showMini) {
						val p = playerViewModel.player
						val d = p?.duration ?: C.TIME_UNSET
						progress = if (p != null && d != C.TIME_UNSET && d > 0)
							(p.currentPosition.toFloat() / d).coerceIn(0f, 1f) else 0f
						delay(500.milliseconds)
					}
				}
				val errorMessageLoad = (messageLoad as? UiState.Error)?.message
				val errorMessage = errorMessagePlay ?: errorMessageLoad
				LaunchedEffect(errorMessage) {
					errorMessage?.let {
						snackbarHostState.showSnackbar(it)
						playerViewModel.clearError()
					}
				}
				Scaffold(
					modifier = Modifier.fillMaxSize(),
					snackbarHost = { SnackbarHost(snackbarHostState) },
					bottomBar = {
						Column {
							AnimatedVisibility(
								visible = showMini,
								enter = slideInVertically{ it },
								exit = slideOutVertically{ it }
							) {
								lastPlaying?.let { item ->
									MiniPlayer(
										player = playerViewModel,
										img = item.imageUrl,
										isPlaying = isPlaying,
										onTogglePlayPause = playerViewModel::togglePlayPause,
										onClick = {
											navController.navigate(Routes.PLAYER) {
												launchSingleTop = true
												popUpTo(Routes.PLAYER)
											}
										}
									)
								}
							}
							NavigationBar (containerColor = MaterialTheme.colorScheme.secondaryContainer){
								NavigationBarItem(
									selected = currentRoute == Routes.PLAYER,
									onClick = {navController.navigate(Routes.PLAYER){
										launchSingleTop = true
										popUpTo(Routes.PLAYER)
									}},
									icon = { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.player)) },
									label = { Text(stringResource(R.string.player)) },
									colors = NavigationBarItemDefaults.colors(
										selectedIconColor = MaterialTheme.colorScheme.primary,
										selectedTextColor = MaterialTheme.colorScheme.primary,
										indicatorColor = MaterialTheme.colorScheme.primaryContainer,
										unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
										unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								)
								NavigationBarItem(
									selected = currentRoute == Routes.SUBSCRIPTIONS,
									onClick = {navController.navigate(Routes.SUBSCRIPTIONS){
										launchSingleTop = true
										popUpTo(Routes.SUBSCRIPTIONS)
									}},
									icon = { Icon(Icons.Default.Subscriptions, contentDescription = stringResource(R.string.subscriptions)) },
									label = { Text(stringResource(R.string.subscriptions)) },
									colors = NavigationBarItemDefaults.colors(
										selectedIconColor = MaterialTheme.colorScheme.primary,
										selectedTextColor = MaterialTheme.colorScheme.primary,
										indicatorColor = MaterialTheme.colorScheme.primaryContainer,
										unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
										unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								)
								NavigationBarItem(
									selected = currentRoute == Routes.EPISODE_LIST,
									onClick = { navController.navigate(Routes.EPISODE_LIST){
										launchSingleTop = true
										popUpTo(Routes.EPISODE_LIST){inclusive = false}
									}
									},
									icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.ep)) },
									label = { Text(stringResource(R.string.ep)) },
									colors = NavigationBarItemDefaults.colors(
										selectedIconColor = MaterialTheme.colorScheme.primary,
										selectedTextColor = MaterialTheme.colorScheme.primary,
										indicatorColor = MaterialTheme.colorScheme.primaryContainer,
										unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
										unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								)
								NavigationBarItem(
									selected = currentRoute == Routes.SONG_LIST,
									onClick = { navController.navigate(Routes.SONG_LIST){
										launchSingleTop = true
										popUpTo(Routes.SONG_LIST){inclusive = false}
									}
									},
									icon = { Icon(Icons.Default.MusicNote, contentDescription = stringResource(R.string.song)) },
									label = { Text(stringResource(R.string.song)) },
									colors = NavigationBarItemDefaults.colors(
										selectedIconColor = MaterialTheme.colorScheme.primary,
										selectedTextColor = MaterialTheme.colorScheme.primary,
										indicatorColor = MaterialTheme.colorScheme.primaryContainer,
										unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
										unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								)
								NavigationBarItem(
									selected = currentRoute== Routes.SETTINGS,
									onClick = {
										navController.navigate(Routes.SETTINGS){
											launchSingleTop = true
											popUpTo(Routes.SETTINGS){inclusive = false}
										}
									},
									icon = {Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))},
									label = { Text(stringResource(R.string.settings)) },
									colors = NavigationBarItemDefaults.colors(
										selectedIconColor = MaterialTheme.colorScheme.primary,
										selectedTextColor = MaterialTheme.colorScheme.primary,
										indicatorColor = MaterialTheme.colorScheme.primaryContainer,
										unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
										unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								)
							}
						}
					}
				) { innerPadding ->
					NavGraph(
						navController = navController,
						mediaViewModel=mediaViewModel,
						playerViewModel=playerViewModel,
						settings=settings,
						modifier = Modifier.padding(innerPadding)
					)
				}
			}
		}
	}
}