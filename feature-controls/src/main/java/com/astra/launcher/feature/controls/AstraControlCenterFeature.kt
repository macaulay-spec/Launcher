package com.astra.launcher.feature.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraMediaCard
import com.astra.launcher.core.design.AstraQuickTile
import com.astra.launcher.core.design.AstraSlider
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraToast
import com.astra.launcher.core.platform.SystemActionFeedback
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.AstraMediaState

/**
 * Spatial Control Plane / Quick Settings (Section 18 & 26; Frames 15, 16, 28).
 * Distinct from iOS Control Center and Samsung Quick Panel: organized into
 * Connectivity Plane, Luminance & Audio Sliders, Hardware Utilities, and Adaptive Media.
 */
@Composable
fun AstraControlCenterScreen(
    deviceStatus: AstraDeviceStatus,
    mediaState: AstraMediaState,
    compactDensity: Boolean,
    initiallyPartial: Boolean = false,
    lastFeedback: SystemActionFeedback?,
    onToggleTorch: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onSystemTileTap: (String) -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var fullPlaneExpanded by remember { mutableStateOf(!initiallyPartial) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header: Time, Connectivity Summary, Device State
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SPATIAL CONTROL PLANE · ${deviceStatus.batteryPercent}%",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary
                )
                Text(
                    text = if (deviceStatus.isOffline) "Offline · Local Shell Active" else "Connected · ${deviceStatus.wifiSsid}",
                    style = AstraTheme.typography.titleL,
                    color = colors.textPrimary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AstraIconButton(
                    glyph = AstraGlyph.NOTIFICATIONS,
                    contentDescriptionLabel = "Open Notifications",
                    onClick = onOpenNotifications
                )
                AstraIconButton(
                    glyph = AstraGlyph.SETTINGS,
                    contentDescriptionLabel = "Open Astra Settings",
                    onClick = onOpenSettings
                )
                AstraIconButton(
                    glyph = AstraGlyph.CLOSE,
                    contentDescriptionLabel = "Close Control Center",
                    onClick = onClose
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PRIMARY CONNECTIVITY ZONE",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Text(
                text = if (fullPlaneExpanded) "Switch to Partial View" else "Expand Full Plane",
                style = AstraTheme.typography.labelS,
                color = colors.accentPrimary,
                modifier = Modifier.clickable { fullPlaneExpanded = !fullPlaneExpanded }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Zone: Wi-Fi, Bluetooth, Mobile Data, Airplane Mode, Hotspot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AstraQuickTile(
                title = "Wi-Fi",
                subtitle = if (deviceStatus.isOffline) "Disconnected" else deviceStatus.wifiSsid,
                glyph = AstraGlyph.WIFI,
                state = if (!deviceStatus.isOffline && deviceStatus.wifiEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                compact = compactDensity,
                isSystemHandoff = true,
                onClick = { onSystemTileTap("wifi") },
                modifier = Modifier.weight(1f)
            )
            AstraQuickTile(
                title = "Bluetooth",
                subtitle = if (deviceStatus.bluetoothEnabled) "Spatial Buds" else "Off",
                glyph = AstraGlyph.BLUETOOTH,
                state = if (deviceStatus.bluetoothEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                compact = compactDensity,
                isSystemHandoff = true,
                onClick = { onSystemTileTap("bluetooth") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AstraQuickTile(
                title = "Mobile Data",
                subtitle = if (deviceStatus.mobileDataEnabled) "5G Standalone" else "Paused",
                glyph = AstraGlyph.CELLULAR,
                state = if (deviceStatus.mobileDataEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                compact = compactDensity,
                isSystemHandoff = true,
                onClick = { onSystemTileTap("mobile_data") },
                modifier = Modifier.weight(1f)
            )
            AstraQuickTile(
                title = "Flashlight",
                subtitle = if (deviceStatus.flashlightEnabled) "Torch Active" else "Ready (Direct API)",
                glyph = AstraGlyph.FLASHLIGHT,
                state = if (deviceStatus.flashlightEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                compact = compactDensity,
                isSystemHandoff = false,
                onClick = onToggleTorch,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sliders: Brightness & Volume
        AstraSlider(
            label = "Display Luminance",
            glyph = AstraGlyph.BRIGHTNESS,
            value = deviceStatus.brightnessFraction,
            onValueChange = onBrightnessChange
        )

        Spacer(modifier = Modifier.height(10.dp))

        AstraSlider(
            label = "Spatial Audio Volume",
            glyph = AstraGlyph.VOLUME,
            value = deviceStatus.volumeFraction,
            onValueChange = onVolumeChange
        )

        if (fullPlaneExpanded) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "SECONDARY HARDWARE & POWER ZONE",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AstraQuickTile(
                    title = "Airplane",
                    subtitle = if (deviceStatus.airplaneModeEnabled) "On" else "System Panel",
                    glyph = AstraGlyph.AIRPLANE,
                    state = if (deviceStatus.airplaneModeEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                    compact = compactDensity,
                    isSystemHandoff = true,
                    onClick = { onSystemTileTap("airplane") },
                    modifier = Modifier.weight(1f)
                )
                AstraQuickTile(
                    title = "Hotspot",
                    subtitle = "Tethering Panel",
                    glyph = AstraGlyph.HOTSPOT,
                    state = if (deviceStatus.hotspotEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                    compact = compactDensity,
                    isSystemHandoff = true,
                    onClick = { onSystemTileTap("hotspot") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AstraQuickTile(
                    title = "Orientation",
                    subtitle = if (deviceStatus.autoRotateEnabled) "Auto-Rotate" else "Portrait Locked",
                    glyph = AstraGlyph.CONTROLS,
                    state = if (deviceStatus.autoRotateEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                    compact = compactDensity,
                    isSystemHandoff = false,
                    onClick = { onSystemTileTap("auto_rotate") },
                    modifier = Modifier.weight(1f)
                )
                AstraQuickTile(
                    title = "Battery Saver",
                    subtitle = if (deviceStatus.isLowBattery) "Recommended" else "Power Settings",
                    glyph = AstraGlyph.BATTERY,
                    state = if (deviceStatus.isLowBattery) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                    compact = compactDensity,
                    isSystemHandoff = true,
                    onClick = { onSystemTileTap("battery_saver") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Persistent Media Card (Section 18 & 26, Frame 28)
        Text(
            text = "ADAPTIVE MEDIA SURFACE",
            style = AstraTheme.typography.labelS,
            color = colors.textTertiary
        )
        Spacer(modifier = Modifier.height(8.dp))
        AstraMediaCard(
            media = mediaState,
            onPlayPause = onMediaPlayPause,
            onSkipNext = onMediaNext
        )

        if (lastFeedback != null) {
            Spacer(modifier = Modifier.height(14.dp))
            AstraToast(
                message = "${lastFeedback.actionTitle}: ${lastFeedback.userMessage}"
            )
        }
    }
}
