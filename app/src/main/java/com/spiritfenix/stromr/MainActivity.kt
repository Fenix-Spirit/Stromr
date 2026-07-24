package com.spiritfenix.stromr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spiritfenix.stromr.navigation.NavGraph
import com.spiritfenix.stromr.navigation.Routes
import com.spiritfenix.stromr.ui.MediaViewModel
import com.spiritfenix.stromr.ui.PlayerViewModel
import com.spiritfenix.stromr.ui.theme.StrömrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StrömrTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val mediaViewModel: MediaViewModel = viewModel()
                val playerViewModel: PlayerViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val errorMessage by playerViewModel.errorMessage.collectAsState()
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
                        NavigationBar {
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
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.song)) },
                                label = { Text(stringResource(R.string.song)) },
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
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        mediaViewModel=mediaViewModel,
                        playerViewModel=playerViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}