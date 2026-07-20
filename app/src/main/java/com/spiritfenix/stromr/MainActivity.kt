package com.spiritfenix.stromr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.spiritfenix.stromr.ui.theme.StrömrTheme
import androidx.navigation.compose.rememberNavController
import com.spiritfenix.stromr.navigation.NavGraph
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.spiritfenix.stromr.navigation.Routes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spiritfenix.stromr.ui.MediaViewModel
import com.spiritfenix.stromr.ui.PlayerViewModel

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
                                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Episodes") },
                                label = { Text("Episodes") },
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
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Tracks") },
                                label = { Text("Tracks") },
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