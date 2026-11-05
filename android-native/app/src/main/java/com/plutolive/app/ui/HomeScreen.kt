package com.plutolive.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onCamera: () -> Unit,
    onViewer: () -> Unit,
    dark: Boolean,
    onToggleTheme: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onToggleTheme) {
                Text(if (dark) "Light mode" else "Dark mode")
            }
        }

        Text("PLUTO", color = MaterialTheme.colorScheme.onBackground, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text("LIVE", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        ModeCard(
            title = "Camera Mode",
            subtitle = "Turn this device into a live camera",
            accent = MaterialTheme.colorScheme.secondary,
            label = "Open",
            onClick = onCamera
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            title = "Viewer Mode",
            subtitle = "Watch another device's camera",
            accent = MaterialTheme.colorScheme.primary,
            label = "Open",
            onClick = onViewer
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    accent: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Text(
            label,
            color = Color(0xFF04202A),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(accent, RoundedCornerShape(14.dp))
                .padding(vertical = 14.dp)
        )
    }
}
