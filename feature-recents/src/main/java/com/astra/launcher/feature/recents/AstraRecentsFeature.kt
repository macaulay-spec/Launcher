package com.astra.launcher.feature.recents

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.design.resolveGlyphForPackage
import com.astra.launcher.core.platform.AstraRecentsRepository
import com.astra.launcher.core.storage.AstraTaskEntry

/**
 * Recents / Multitasking Overview (Section 21 & Frame 17).
 * Uses honest task metadata without faking unauthorized live app framebuffers.
 */
@Composable
fun AstraRecentsScreen(
    onRestoreTask: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    val tasks = remember { mutableStateListOf<AstraTaskEntry>().apply { addAll(AstraRecentsRepository.getRecentTasks()) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RECENT TASKS · MULTITASKING",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary
                )
                Text(
                    text = "Active Workspace Continuum",
                    style = AstraTheme.typography.headlineM,
                    color = colors.textPrimary
                )
            }
            AstraIconButton(
                glyph = AstraGlyph.CLOSE,
                contentDescriptionLabel = "Close Recents",
                onClick = onClose
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Platform Truth: Live task framebuffers require OEM QuickStep signature privileges. Astra displays verified task continuity cards with instant restore and split-screen handoff.",
            style = AstraTheme.typography.bodyS,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (tasks.isEmpty()) {
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.SOFT_TRANSLUCENT
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Active Tasks", style = AstraTheme.typography.titleM, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("All recent tasks have been cleared.", style = AstraTheme.typography.bodyS, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surfaceFloating)
                            .clickable { tasks.addAll(AstraRecentsRepository.getRecentTasks()) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("Restore Recent Session", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                    }
                }
            }
        } else {
            tasks.forEachIndexed { idx, task ->
                val isPrimaryFocus = idx == 0
                val taskAccent = Color(task.accentHex)

                AstraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    family = if (isPrimaryFocus) AstraSurfaceFamily.SOFT_TRANSLUCENT else AstraSurfaceFamily.CLEAR_ATMOSPHERIC,
                    shape = AstraTheme.shapes.elevatedSheet,
                    state = if (isPrimaryFocus) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                    onClick = { onRestoreTask(task.packageName) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(taskAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AstraVectorIcon(
                                        glyph = resolveGlyphForPackage(task.packageName, task.appName),
                                        size = 20.dp,
                                        tint = taskAccent,
                                        active = true
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.appName,
                                            style = AstraTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary
                                        )
                                        if (isPrimaryFocus) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "CURRENT FOCUS",
                                                style = AstraTheme.typography.labelS,
                                                color = colors.accentPrimary
                                            )
                                        }
                                    }
                                    Text(
                                        text = task.lastActiveLabel,
                                        style = AstraTheme.typography.labelS,
                                        color = colors.textTertiary
                                    )
                                }
                            }
                            AstraIconButton(
                                glyph = AstraGlyph.CLOSE,
                                contentDescriptionLabel = "Dismiss Task",
                                onClick = { tasks.remove(task) },
                                size = 32.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Structured Task Context Surface (never a fake screenshot)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AstraTheme.shapes.compactCard)
                                .background(colors.surfaceFloating.copy(alpha = 0.72f))
                                .border(1.dp, colors.borderSubtle, AstraTheme.shapes.compactCard)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = task.contextSummary,
                                    style = AstraTheme.typography.bodyM,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tap card to restore",
                                        style = AstraTheme.typography.labelS,
                                        color = colors.accentPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (task.supportsSplitScreen) {
                                            Text(
                                                text = "Split Screen",
                                                style = AstraTheme.typography.labelS,
                                                color = colors.textSecondary,
                                                modifier = Modifier.clickable { onRestoreTask(task.packageName) }
                                            )
                                        }
                                        Text(
                                            text = "App Info",
                                            style = AstraTheme.typography.labelS,
                                            color = colors.textSecondary,
                                            modifier = Modifier.clickable { onOpenAppInfo(task.packageName) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
