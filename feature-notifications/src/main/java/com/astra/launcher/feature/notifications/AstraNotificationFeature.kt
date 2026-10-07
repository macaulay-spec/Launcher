package com.astra.launcher.feature.notifications

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraNotificationCard
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.storage.AstraNotificationEntry
import com.astra.launcher.core.storage.NotificationPriorityBucket

/**
 * Notification Shade Surface (Section 17, 24, 34.3; Frames 13, 14, 25, 26, 27).
 */
@Composable
fun AstraNotificationShadeScreen(
    notifications: List<AstraNotificationEntry>,
    hasNotificationListenerPermission: Boolean,
    hideSensitiveContent: Boolean,
    initiallyCollapsed: Boolean = false,
    onOpenNotificationApp: (String) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearAll: () -> Unit,
    onRestoreSampleNotifications: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var expanded by remember { mutableStateOf(!initiallyCollapsed) }
    var silentExpanded by remember { mutableStateOf(false) }
    var permissionDeniedSimulated by remember { mutableStateOf(false) }

    val urgent = notifications.filter { it.bucket == NotificationPriorityBucket.URGENT }
    val regular = notifications.filter { it.bucket == NotificationPriorityBucket.REGULAR }
    val silent = notifications.filter { it.bucket == NotificationPriorityBucket.SILENT }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WEDNESDAY, OCT 7 · 09:41",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary
                )
                Text(
                    text = if (expanded) "Notification Stream" else "Collapsed Shade Summary",
                    style = AstraTheme.typography.headlineM,
                    color = colors.textPrimary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AstraIconButton(
                    glyph = AstraGlyph.CONTROLS,
                    contentDescriptionLabel = "Switch to Control Center",
                    onClick = onOpenControlCenter
                )
                AstraIconButton(
                    glyph = AstraGlyph.CLOSE,
                    contentDescriptionLabel = "Close Notification Shade",
                    onClick = onClose
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Capability & Permission Pre-Explanation / Handoff Card (Section 24; Frames 25, 26, 27)
        if (!hasNotificationListenerPermission) {
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                state = if (permissionDeniedSimulated) AstraComponentState.ERROR else AstraComponentState.ACTIVE
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (permissionDeniedSimulated) {
                                "PERMISSION DENIED · PREVIEW FALLBACK ACTIVE"
                            } else {
                                "NOTIFICATION ACCESS · PRE-EXPLANATION"
                            },
                            style = AstraTheme.typography.labelS,
                            color = if (permissionDeniedSimulated) colors.warning else colors.accentPrimary
                        )
                        Text(
                            text = "NotificationListenerService",
                            style = AstraTheme.typography.labelS,
                            color = colors.textTertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (permissionDeniedSimulated) {
                            "Live system notification mirroring is disabled because access was declined. Astra continues to work normally with local reminders and preview cards."
                        } else {
                            "Why Astra asks: Granting Notification Access lets Astra group priority messages and show real-time badges. If declined, Home and Search still work normally without reading notifications."
                        },
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.accentPrimary)
                                .clickable { onRequestNotificationAccess() }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Open System Consent",
                                style = AstraTheme.typography.labelS,
                                color = colors.surfaceBase
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surfaceFloating)
                                .clickable { permissionDeniedSimulated = !permissionDeniedSimulated }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (permissionDeniedSimulated) "Reset State" else "Keep Preview Mode",
                                style = AstraTheme.typography.labelS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Toggle between Collapsed (Frame 13) and Expanded (Frame 14)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${notifications.size} ACTIVE NOTIFICATIONS",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (expanded) "Collapse Stack" else "Expand All (${notifications.size})",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
                if (notifications.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        style = AstraTheme.typography.labelS,
                        color = colors.textSecondary,
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notifications.isEmpty()) {
            // Empty Notification State (Section 28 & Frame 33)
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.SOFT_TRANSLUCENT
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Atmosphere is Quiet",
                        style = AstraTheme.typography.titleM,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No urgent or pending notifications require your attention right now.",
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surfaceFloating)
                            .clickable { onRestoreSampleNotifications() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Restore Sample Stream",
                            style = AstraTheme.typography.labelS,
                            color = colors.accentPrimary
                        )
                    }
                }
            }
        } else if (!expanded) {
            // Frame 13: Collapsed summary stack
            urgent.firstOrNull()?.let { topItem ->
                AstraNotificationCard(
                    item = topItem,
                    hideSensitiveContent = hideSensitiveContent,
                    onOpen = { onOpenNotificationApp(topItem.packageName) },
                    onDismiss = { onDismissNotification(topItem.id) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.CLEAR_ATMOSPHERIC,
                onClick = { expanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${(notifications.size - 1).coerceAtLeast(1)} grouped notifications",
                        style = AstraTheme.typography.bodyS.copy(fontWeight = FontWeight.Medium),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tap to expand",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary
                    )
                }
            }
        } else {
            // Frame 14: Expanded 3-tier hierarchy
            if (urgent.isNotEmpty()) {
                Text(
                    text = "PRIORITY & URGENT",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                urgent.forEach { item ->
                    AstraNotificationCard(
                        item = item,
                        hideSensitiveContent = hideSensitiveContent,
                        onOpen = { onOpenNotificationApp(item.packageName) },
                        onDismiss = { onDismissNotification(item.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (regular.isNotEmpty()) {
                Text(
                    text = "REGULAR UPDATES",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                regular.forEach { item ->
                    AstraNotificationCard(
                        item = item,
                        hideSensitiveContent = hideSensitiveContent,
                        onOpen = { onOpenNotificationApp(item.packageName) },
                        onDismiss = { onDismissNotification(item.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (silent.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { silentExpanded = !silentExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SILENT & LOW PRIORITY (${silent.size})",
                        style = AstraTheme.typography.labelS,
                        color = colors.textTertiary
                    )
                    Text(
                        text = if (silentExpanded) "Hide" else "Show",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary
                    )
                }
                if (silentExpanded) {
                    silent.forEach { item ->
                        AstraNotificationCard(
                            item = item,
                            hideSensitiveContent = hideSensitiveContent,
                            onOpen = { onOpenNotificationApp(item.packageName) },
                            onDismiss = { onDismissNotification(item.id) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
