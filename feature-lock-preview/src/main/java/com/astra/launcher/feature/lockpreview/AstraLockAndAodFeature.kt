package com.astra.launcher.feature.lockpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraBrandMark
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraClock
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraNotificationCard
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraSegmentedControl
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.AstraNotificationEntry
import com.astra.launcher.core.storage.AstraWallpaperId

/**
 * Lock Screen Concept & Companion Experience (Section 11 & 34.2; Frame 36).
 */
@Composable
fun AstraLockScreen(
    wallpaperId: AstraWallpaperId,
    clockStyle: AstraClockStyle,
    notifications: List<AstraNotificationEntry>,
    deviceStatus: AstraDeviceStatus,
    hideSensitiveOnLock: Boolean,
    onSelectClockStyle: (AstraClockStyle) -> Unit,
    onToggleTorch: () -> Unit,
    onOpenCamera: () -> Unit,
    onSwitchToAod: () -> Unit,
    onUnlockToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var customizeMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: quiet status indicators + optional weather/condition chip
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASTRA LOCK SURFACE",
                    style = AstraTheme.typography.labelS,
                    color = colors.textSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "AOD Preview",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary,
                        modifier = Modifier.clickable { onSwitchToAod() }
                    )
                    Text(
                        text = "${deviceStatus.batteryPercent}%",
                        style = AstraTheme.typography.labelS,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weather / Condition Chip
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surfaceGlass)
                    .border(1.dp, colors.borderSubtle, CircleShape)
                    .clickable { customizeMode = !customizeMode }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AstraVectorIcon(glyph = AstraGlyph.SPARK, size = 14.dp, tint = colors.accentPrimary, active = true)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "18°C · ${wallpaperId.title} · Tap to Customize Clock",
                    style = AstraTheme.typography.labelS,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Large Adaptive Clock (Section 11)
            AstraClock(
                style = clockStyle,
                upperLuminance = wallpaperId.upperRegionLuminance,
                isLockScreen = true
            )

            if (customizeMode) {
                Spacer(modifier = Modifier.height(14.dp))
                AstraSegmentedControl(
                    items = AstraClockStyle.entries,
                    selectedItem = clockStyle,
                    labelProvider = { it.label },
                    onSelect = onSelectClockStyle
                )
            }
        }

        // Lower Center: Priority Grouped Notifications (Privacy-gated per Section 11)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (hideSensitiveOnLock) {
                    "PRIORITY NOTIFICATIONS · PRIVATE CONTENT REDACTED"
                } else {
                    "PRIORITY NOTIFICATIONS"
                },
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )

            notifications.take(2).forEach { notif ->
                AstraNotificationCard(
                    item = notif,
                    hideSensitiveContent = hideSensitiveOnLock,
                    onOpen = onUnlockToHome,
                    onDismiss = {}
                )
            }
        }

        // Bottom: Left quick access, Biometric/Unlock affordance, Right quick access
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AstraIconButton(
                glyph = AstraGlyph.FLASHLIGHT,
                contentDescriptionLabel = "Lock Screen Torch",
                onClick = onToggleTorch,
                state = if (deviceStatus.flashlightEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                size = 48.dp
            )

            // Biometric / Unlock Orbital Affordance
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onUnlockToHome() }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceGlassStrong)
                        .border(1.5.dp, colors.accentPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AstraBrandMark(size = 32.dp, luminousMoment = true)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to Unlock into Astra Home",
                    style = AstraTheme.typography.labelS,
                    color = colors.textSecondary
                )
            }

            AstraIconButton(
                glyph = AstraGlyph.CAMERA,
                contentDescriptionLabel = "Lock Screen Camera",
                onClick = onOpenCamera,
                size = 48.dp
            )
        }
    }
}

/**
 * Always-On Display (AOD) Companion Surface (Section 12 & 34.5; Frame 37).
 * Restrained low-luminance surface with burn-in pixel shift strategy.
 */
@Composable
fun AstraAodScreen(
    clockStyle: AstraClockStyle,
    batteryPercent: Int,
    urgentNotificationCount: Int,
    onWakeToLock: () -> Unit,
    onWakeToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pixelShiftStep by remember { mutableIntStateOf(0) }
    val offsetX = ((pixelShiftStep % 3) - 1) * 6
    val offsetY = (((pixelShiftStep / 3) % 3) - 1) * 8

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AstraPalette.AstraBlack)
            .clickable { onWakeToLock() }
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(offsetX, offsetY) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AstraBrandMark(
                size = 28.dp,
                luminousMoment = false,
                tint = Color(0xFF8A92A3),
                accent = Color(0xFF568EA6)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "09:41",
                style = AstraTheme.typography.displayXl,
                color = Color(0xFFD8DBE1).copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "WEDNESDAY · OCT 7 · $batteryPercent%",
                style = AstraTheme.typography.labelL,
                color = Color(0xFF949BA8).copy(alpha = 0.68f)
            )
            if (urgentNotificationCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF333947), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7DD3FC).copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$urgentNotificationCount essential notification${if (urgentNotificationCount > 1) "s" else ""}",
                        style = AstraTheme.typography.labelS,
                        color = Color(0xFF949BA8)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Burn-in shift step #$pixelShiftStep",
                style = AstraTheme.typography.labelS,
                color = Color(0xFF565D6B),
                modifier = Modifier.clickable { pixelShiftStep++ }
            )
            Text(
                text = "Wake to Home →",
                style = AstraTheme.typography.labelS,
                color = Color(0xFF7DD3FC).copy(alpha = 0.75f),
                modifier = Modifier.clickable { onWakeToHome() }
            )
        }
    }
}

/**
 * Charging Experience Moment (Section 27 & Frame 29).
 * Subtle center orbital pulse, battery percentage, estimated charging state.
 */
@Composable
fun AstraChargingOverlay(
    batteryPercent: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors

    AstraCard(
        modifier = modifier.fillMaxWidth(),
        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
        shape = AstraTheme.shapes.heroPanel,
        state = AstraComponentState.ACTIVE,
        onClick = onDismiss
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AstraBrandMark(size = 46.dp, luminousMoment = true)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ORBITAL FAST CHARGE ACTIVE",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary
                    )
                    Text(
                        text = "$batteryPercent% · Optimal Thermal Envelope",
                        style = AstraTheme.typography.titleM.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Estimated full charge in 24 minutes · Tap to dismiss",
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary
                    )
                }
            }
            AstraIconButton(
                glyph = AstraGlyph.CLOSE,
                contentDescriptionLabel = "Dismiss Charging Moment",
                onClick = onDismiss,
                size = 34.dp
            )
        }
    }
}
