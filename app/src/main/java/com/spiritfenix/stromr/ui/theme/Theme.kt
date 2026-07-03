package com.spiritfenix.stromr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Base
val Black900 = Color(0xFF0A0A0A)
val Black800 = Color(0xFF141414)
val Black700 = Color(0xFF2A2A2A)
val White100 = Color(0xFFF5F5F5)
val White200 = Color(0xFFE0E0E0)

// Accent — ember orange
val Ember      = Color(0xFFE8621A)
val EmberLight = Color(0xFFF4A96A)
val EmberDim   = Color(0xFF7A3410)

private val DarkColorScheme = darkColorScheme(
    primary          = Ember,
    onPrimary        = White100,
    primaryContainer = EmberDim,
    onPrimaryContainer = EmberLight,

    background       = Black900,
    onBackground     = White100,

    surface          = Black800,
    onSurface        = White100,
    surfaceVariant   = Black700,
    onSurfaceVariant = White200,

    secondary        = EmberLight,
    onSecondary      = Black900,

    outline          = Black700,
)

private val LightColorScheme = lightColorScheme(
    primary          = Ember,
    onPrimary        = White100,
    primaryContainer = EmberLight,
    onPrimaryContainer = EmberDim,

    background       = White100,
    onBackground     = Black900,

    surface          = Color(0xFFFFFFFF),
    onSurface        = Black900,
    surfaceVariant   = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF555555),

    secondary        = EmberDim,
    onSecondary      = White100,

    outline          = Color(0xFFD0D0D0),
)

@Composable
fun StrömrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}