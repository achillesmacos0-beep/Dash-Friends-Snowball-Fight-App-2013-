package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DashGreenLight,
    onPrimary = Color.Black,
    primaryContainer = DashGreenDark,
    onPrimaryContainer = Color.White,
    secondary = PbsCyanLight,
    onSecondary = Color.Black,
    secondaryContainer = PbsCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = WinterYellow,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DashGreen,
    onPrimary = Color.White,
    primaryContainer = DashGreenLight,
    onPrimaryContainer = Color(0xFF1B3B00),
    secondary = PbsCyan,
    onSecondary = Color.White,
    secondaryContainer = SnowBlue,
    onSecondaryContainer = Color(0xFF003650),
    tertiary = WinterOrange,
    onTertiary = Color.White,
    background = Color(0xFFF0F9FF),
    surface = Color.White,
    onBackground = Color(0xFF0F1E29),
    onSurface = Color(0xFF0F1E29)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
