package com.spiritfenix.stromr.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.ui.*

/**
 * Navigation graph for the app.
 * @see Routes
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    mediaViewModel: MediaViewModel
){
	val openPlayer: (String) -> Unit = { id ->
		playerViewModel.play(mediaViewModel.itemsFrom(id))
		navController.navigate(Routes.PLAYER) { launchSingleTop = true }
	}
	val addToQueue: (String) -> Unit = { id ->
		mediaViewModel.findById(id)?.let { playerViewModel.addToQueue(it) }
	}
    NavHost(
        navController=navController,
        startDestination = Routes.EPISODE_LIST,
        modifier = modifier
    ){
        composable(
            Routes.EPISODE_LIST,
            enterTransition = { slideInHorizontally(animationSpec = tween(200)) },
            exitTransition = {fadeOut(animationSpec = tween(100))}
        ){
            ListMediaScreen(
                filter = { it is MediaItem.Episode },
                onItemTap= openPlayer,
                onAddToQueue = addToQueue,
                viewModel=mediaViewModel
            )
        }
        composable(
            Routes.SONG_LIST,
            enterTransition = { slideInHorizontally(animationSpec = tween(200)){it} },
            exitTransition = {fadeOut(animationSpec = tween(100))}
        ){
            ListMediaScreen(
                filter = { it is MediaItem.Song},
                onItemTap= openPlayer,
                onAddToQueue = addToQueue,
                viewModel=mediaViewModel
            )
        }
        composable(
            route = Routes.PLAYER,
            enterTransition = { slideInVertically(animationSpec = tween(200)){it} },
            exitTransition = { fadeOut(animationSpec = tween(100)) }
        ){
            PlayerScreen(playerViewModel=playerViewModel,mediaViewModel=mediaViewModel)
        }
		composable(
			route = Routes.SUBSCRIPTIONS,
			enterTransition = { slideInVertically(animationSpec = tween(200)){it} },
			exitTransition = {fadeOut(animationSpec = tween(100))}
		){
			SubscriptionsScreen(mediaViewModel)
		}
    }
}