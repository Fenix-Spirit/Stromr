package com.spiritfenix.stromr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import com.spiritfenix.stromr.ui.theme.StrömrTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.spiritfenix.stromr.ui.ListEpisodesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StrömrTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ListEpisodesScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}