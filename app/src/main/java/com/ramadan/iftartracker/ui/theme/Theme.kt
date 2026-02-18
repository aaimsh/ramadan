package com.ramadan.iftartracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFFFD700)
val GoldDark = Color(0xFFC7A600)
val DeepPurple = Color(0xFF1A0533)
val NightBlue = Color(0xFF0D1B2A)
val NightBlueSurface = Color(0xFF152238)
val TealAccent = Color(0xFF00BFA5)
val WarmWhite = Color(0xFFFFF8E7)
val SunsetOrange = Color(0xFFFF6F3C)
val MidnightCard = Color(0xFF1B2D45)

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = NightBlue,
    primaryContainer = GoldDark,
    secondary = TealAccent,
    onSecondary = NightBlue,
    tertiary = SunsetOrange,
    background = NightBlue,
    surface = NightBlueSurface,
    surfaceVariant = MidnightCard,
    onBackground = WarmWhite,
    onSurface = WarmWhite,
    onSurfaceVariant = Color(0xFFBBC5D5),
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPurple,
    onPrimary = WarmWhite,
    primaryContainer = Gold,
    secondary = TealAccent,
    tertiary = SunsetOrange,
    background = WarmWhite,
    surface = Color(0xFFFFF0D0),
    surfaceVariant = Color(0xFFFFE8B5),
    onBackground = DeepPurple,
    onSurface = DeepPurple,
    onSurfaceVariant = Color(0xFF4A3960),
)

@Composable
fun IftarTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
