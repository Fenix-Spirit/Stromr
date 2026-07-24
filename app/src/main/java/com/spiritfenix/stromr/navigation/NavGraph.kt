package com.spiritfenix.stromr.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.ui.ListMediaScreen
import com.spiritfenix.stromr.ui.MediaViewModel
import com.spiritfenix.stromr.ui.PlayerScreen
import com.spiritfenix.stromr.ui.PlayerViewModel

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
                onItemTap= {mediaId->navController.navigate(Routes.player(mediaId))},
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
                onItemTap= {mediaId->navController.navigate(Routes.player(mediaId))},
                viewModel=mediaViewModel
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(navArgument("mediaId") { type = NavType.IntType }),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ){ backStackEntry ->
            val mediaId = backStackEntry.arguments?.getInt("mediaId")?: return@composable
            PlayerScreen(mediaId=mediaId,playerViewModel=playerViewModel,mediaViewModel=mediaViewModel)
        }
    }
}