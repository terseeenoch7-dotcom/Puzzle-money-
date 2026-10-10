package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameDarkColorScheme = darkColorScheme(
    primary = GameGold,
    onPrimary = Color(0xFF352000),
    primaryContainer = GameSurfaceElevated,
    onPrimaryContainer = GameGoldLight,
    secondary = GameCyan,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = GameSurfaceDark,
    onSecondaryContainer = GameCyan,
    tertiary = GameMagenta,
    onTertiary = Color.White,
    background = GameBackgroundDark,
    onBackground = TextWhite,
    surface = GameSurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = GameSurfaceElevated,
    onSurfaceVariant = TextSubtitle,
    outline = GameCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For a game with vibrant neon 3D aesthetic, use consistent GameDarkColorScheme
    MaterialTheme(
        colorScheme = GameDarkColorScheme,
        typography = Typography,
        content = content
    )
}
