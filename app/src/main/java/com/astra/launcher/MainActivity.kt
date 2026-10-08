package com.astra.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.astra.launcher.ui.home.HomeScreen
import com.astra.launcher.ui.theme.AstraTheme

/**
 * Astra Launcher entry point.
 * Registered as the default HOME (launcher) activity — see AndroidManifest.xml.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstraTheme(darkTheme = true) {
                HomeScreen()
            }
        }
    }
}
