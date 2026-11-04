package com.plutolive.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

fun randomCode(): String = (1..6).map { ALPHABET[Random.nextInt(ALPHABET.length)] }.joinToString("")

@Composable
fun Card(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Panel, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(title.uppercase(), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(Modifier.padding(top = 10.dp)) { content() }
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color(0xFF04202A)),
        modifier = Modifier.fillMaxWidth()
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun DangerButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48), contentColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) { Text(text, fontWeight = FontWeight.Bold) }
}
