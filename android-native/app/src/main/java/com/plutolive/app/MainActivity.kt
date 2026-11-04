package com.plutolive.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.plutolive.app.ui.CameraScreen
import com.plutolive.app.ui.HomeScreen
import com.plutolive.app.ui.PlutoTheme
import com.plutolive.app.ui.ViewerScreen

class MainActivity : ComponentActivity() {

    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        permissions.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
        setContent { PlutoTheme { App() } }
    }
}

@Composable
private fun App() {
    var screen by remember { mutableStateOf("home") }
    when (screen) {
        "camera" -> CameraScreen(onBack = { screen = "home" })
        "viewer" -> ViewerScreen(onBack = { screen = "home" })
        else -> HomeScreen(
            onCamera = { screen = "camera" },
            onViewer = { screen = "viewer" }
        )
    }
}
