package com.foodpal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0B1118),
    secondary = Color(0xFFB9F6CA),
    tertiary = Color(0xFFFFD8A8),
    background = Color(0xFF0F1318),
    onBackground = Color(0xFFE6E9EE),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6E9EE),
    surfaceVariant = Color(0xFF1F2630),
    onSurfaceVariant = Color(0xFFC3C9D2),
    outline = Color(0xFF3A4250),
)

@Composable
fun FoodPalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content,
    )
}
