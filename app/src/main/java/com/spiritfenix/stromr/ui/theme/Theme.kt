package com.spiritfenix.stromr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.spiritfenix.stromr.R

@Composable
fun StrömrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorResource(R.color.ember),
            onPrimary = colorResource(R.color.white_100),
            primaryContainer = colorResource(R.color.ember_dim),
            onPrimaryContainer = colorResource(R.color.ember_light),

            background = colorResource(R.color.black_900),
            onBackground = colorResource(R.color.white_100),

            surface = colorResource(R.color.black_800),
            onSurface = colorResource(R.color.white_100),
            surfaceVariant = colorResource(R.color.black_700),
            onSurfaceVariant = colorResource(R.color.white_200),

            secondary = colorResource(R.color.ember_light),
            onSecondary = colorResource(R.color.black_900),

            outline = colorResource(R.color.black_700),
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.ember),
            onPrimary = colorResource(R.color.white_100),
            primaryContainer = colorResource(R.color.ember_light),
            onPrimaryContainer = colorResource(R.color.ember_dim),

            background = colorResource(R.color.white_100),
            onBackground = colorResource(R.color.black_900),

            surface = colorResource(R.color.white),
            onSurface = colorResource(R.color.black_900),
            surfaceVariant = colorResource(R.color.grey_f0),
            onSurfaceVariant = colorResource(R.color.grey_55),

            secondary = colorResource(R.color.ember_dim),
            onSecondary = colorResource(R.color.white_100),

            outline = colorResource(R.color.grey_d0),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}