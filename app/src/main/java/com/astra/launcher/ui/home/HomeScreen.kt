package com.astra.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.astra.launcher.ui.theme.AstraColors

/**
 * Astra home — scaffold screen (Phase 6 kickoff).
 * Full workspace (icons, folders, dock, widgets) lands per docs/02_screen_specs.md §2.
 */
@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AstraColors.InkBackground),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "09:41",
                style = MaterialTheme.typography.displayLarge,
                color = AstraColors.Starlight,
            )
            Text(
                text = "Astra Launcher — the default UI",
                style = MaterialTheme.typography.bodyLarge,
                color = AstraColors.Muted,
            )
        }
        // Dock placeholder (spec: translucent blur bar, radius 28, bottom-pinned)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(28.dp))
                .background(AstraColors.SurfaceRaised)
                .padding(vertical = 16.dp),
        )
    }
}
