package com.rib.progressiverecords.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Gray900,
    primaryVariant = Gray800,
    secondary = Orange700,
    secondaryVariant = Orange400,
    background = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color.White,
    primaryVariant = Gray400,
    secondary = Orange700,
    secondaryVariant = Orange400,
    background = Gray300,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.Black

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun ProgressiveRecordsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}