package com.spiritfenix.stromr.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.ui.ListMediaScreen
import com.spiritfenix.stromr.ui.PlayerScreen

@Composable
fun NavGraph(navController: NavHostController,modifier: Modifier = Modifier){
    NavHost(
        navController=navController,
        startDestination = Routes.EPISODE_LIST,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) }
    ){
        composable(Routes.EPISODE_LIST){
            ListMediaScreen(
                filter = { it is MediaItem.Episode },
                onItemTap= {mediaId->navController.navigate(Routes.player(mediaId))}
            )
        }
        composable(Routes.SONG_LIST){
            ListMediaScreen(
                filter = { it is MediaItem.Song},
                onItemTap= {mediaId->navController.navigate(Routes.player(mediaId))}
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(navArgument("mediaId") { type = NavType.IntType })
        ){ backStackEntry ->
            val mediaId = backStackEntry.arguments?.getInt("mediaId")?: return@composable
            PlayerScreen(mediaId=mediaId)
        }
    }
}