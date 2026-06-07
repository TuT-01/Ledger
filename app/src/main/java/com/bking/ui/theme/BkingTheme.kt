package com.bking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7B61FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF241044),
    secondary = Color(0xFF00A6A6),
    tertiary = Color(0xFFE6B84E),
    background = Color(0xFFF8F3FF),
    onBackground = Color(0xFF20172B),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF20172B),
    surfaceVariant = Color(0xFFE9DFF2),
    onSurfaceVariant = Color(0xFF51465F),
    error = Color(0xFFFF6B8A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC7A6FF),
    onPrimary = Color(0xFF231139),
    primaryContainer = Color(0xFF5B3A86),
    onPrimaryContainer = Color(0xFFF1E5FF),
    secondary = Color(0xFF69E6E6),
    onSecondary = Color(0xFF052B2D),
    tertiary = Color(0xFFFFD36E),
    onTertiary = Color(0xFF352400),
    background = Color(0xFF15101F),
    onBackground = Color(0xFFF4ECFF),
    surface = Color(0xFF21172D),
    onSurface = Color(0xFFF4ECFF),
    surfaceVariant = Color(0xFF31243F),
    onSurfaceVariant = Color(0xFFD8C7E8),
    error = Color(0xFFFF8AA4)
)

@Composable
fun BkingTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
