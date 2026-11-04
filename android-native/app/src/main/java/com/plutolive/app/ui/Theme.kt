package com.plutolive.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF22D3EE)
val DeepBg = Color(0xFF05070F)
val Panel = Color(0xFF0E1424)
val Muted = Color(0xFF93A1C0)
val LiveGreen = Color(0xFF22C55E)

private val scheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF04202A),
    background = DeepBg,
    onBackground = Color(0xFFEAF0FF),
    surface = Panel,
    onSurface = Color(0xFFEAF0FF),
    secondary = LiveGreen
)

@Composable
fun PlutoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
