package com.plutolive.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onCamera: () -> Unit, onViewer: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(DeepBg)
            .padding(20.dp)
    ) {
        Text("PLUTO", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text("LIVE", color = Cyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        ModeCard(
            title = "Camera Mode",
            subtitle = "Turn this device into a live camera",
            accent = LiveGreen,
            onClick = onCamera
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            title = "Viewer Mode",
            subtitle = "Watch another device's camera",
            accent = Cyan,
            onClick = onViewer
        )
    }
}

@Composable
private fun ModeCard(title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Text(
            "Open",
            color = Color(0xFF04202A),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .background(accent, RoundedCornerShape(14.dp))
                .padding(vertical = 14.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
