package com.plutolive.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF22D3EE)
val LiveGreen = Color(0xFF22C55E)

private val DarkScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF04202A),
    secondary = LiveGreen,
    background = Color(0xFF05070F),
    onBackground = Color(0xFFEAF0FF),
    surface = Color(0xFF0E1424),
    onSurface = Color(0xFFEAF0FF),
    onSurfaceVariant = Color(0xFF93A1C0)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0891B2),
    onPrimary = Color.White,
    secondary = Color(0xFF16A34A),
    background = Color(0xFFF2F6FB),
    onBackground = Color(0xFF0B1220),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0B1220),
    onSurfaceVariant = Color(0xFF5A6B85)
)

@Composable
fun PlutoTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        content = content
    )
}
