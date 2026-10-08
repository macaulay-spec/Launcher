package com.astra.launcher.feature.widgets

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.platform.AstraWidgetHostManager
import com.astra.launcher.core.platform.InstalledWidgetProvider
import com.astra.launcher.core.storage.WorkspaceCellItem

/**
 * Genuine Android AppWidgetHost Cell Container (Section 13).
 * Hosts a real bound Android `AppWidgetHostView` inside the 2D workspace coordinate grid.
 * Never simulates fake weather/calendar/music widget content.
 */
@Composable
fun AstraBoundWidgetHostCell(
    item: WorkspaceCellItem,
    cellWidthDp: Int,
    cellHeightDp: Int,
    isEditMode: Boolean,
    palette: AstraPalette,
    widgetHostManager: AstraWidgetHostManager,
    onResizeWidget: (newSpanX: Int, newSpanY: Int) -> Unit,
    onRebindRequest: (WorkspaceCellItem) -> Unit,
    onRemoveWidget: (WorkspaceCellItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val boundInfo = remember(item.appWidgetId, item.widgetProvider) {
        widgetHostManager.getBoundProviderInfo(item.appWidgetId)
    }

    val totalWidthDp = (cellWidthDp * item.spanX).coerceAtLeast(80)
    val totalHeightDp = (cellHeightDp * item.spanY).coerceAtLeast(70)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .clip(AstraShapes.CardMedium)
            .let { mod ->
                if (isEditMode) {
                    mod.border(1.5.dp, palette.primaryAccent, AstraShapes.CardMedium)
                } else mod
            }
    ) {
        if (boundInfo != null && item.appWidgetId >= 0) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    widgetHostManager.createWidgetHostView(context, item.appWidgetId)
                        ?: FrameLayout(ctx)
                },
                update = { view ->
                    if (view is AppWidgetHostView) {
                        try {
                            val opts = Bundle().apply {
                                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, totalWidthDp)
                                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, totalWidthDp)
                                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, totalHeightDp)
                                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, totalHeightDp)
                            }
                            view.updateAppWidgetOptions(opts)
                        } catch (_: Throwable) {
                        }
                    }
                }
            )
        } else {
            // Honest Recovery State when widget binding is pending or provider is unavailable (Section 13 & 31)
            AstraSurfaceCard(
                palette = palette,
                useGlass = true,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = item.label.ifBlank { "Android Widget" },
                            style = AstraTypography.SectionHeader,
                            color = palette.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Widget requires Android bind permission or provider was updated.",
                            style = AstraTypography.Caption,
                            color = palette.secondaryText
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.clickable { onRebindRequest(item) },
                            shape = AstraShapes.ChipPill,
                            color = palette.primaryAccent.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, palette.primaryAccent)
                        ) {
                            Text(
                                text = "Bind Widget",
                                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.primaryAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.clickable { onRemoveWidget(item) },
                            shape = AstraShapes.ChipPill,
                            color = palette.elevatedSurface,
                            border = BorderStroke(1.dp, palette.hairlineBorder)
                        ) {
                            Text(
                                text = "Remove",
                                style = AstraTypography.Caption,
                                color = palette.warningTone,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Edit Mode Resize & Remove overlay controls
        if (isEditMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(AstraShapes.ChipPill)
                    .background(palette.obsidian0.copy(alpha = 0.88f))
                    .border(1.dp, palette.primaryAccent, AstraShapes.ChipPill)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.spanX}×${item.spanY}",
                    style = AstraTypography.MonoMetric,
                    color = palette.primaryText
                )
                Text(
                    text = "W+",
                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryAccent,
                    modifier = Modifier.clickable {
                        val nextX = if (item.spanX >= 4) 1 else item.spanX + 1
                        onResizeWidget(nextX, item.spanY)
                    }
                )
                Text(
                    text = "H+",
                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryAccent,
                    modifier = Modifier.clickable {
                        val nextY = if (item.spanY >= 4) 1 else item.spanY + 1
                        onResizeWidget(item.spanX, nextY)
                    }
                )
                Text(
                    text = "✕",
                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = palette.dangerTone,
                    modifier = Modifier.clickable { onRemoveWidget(item) }
                )
            }
        }
    }
}

/**
 * Genuine Android Widget Picker Overlay (Section 13).
 * Lists real `AppWidgetProviderInfo`s discovered from `AppWidgetManager.installedProviders`.
 */
@Composable
fun AstraWidgetPickerSheet(
    palette: AstraPalette,
    widgetHostManager: AstraWidgetHostManager,
    onSelectProvider: (InstalledWidgetProvider) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val providers = remember(widgetHostManager) {
        widgetHostManager.queryInstalledWidgetProviders()
    }

    AstraSurfaceCard(
        palette = palette,
        useGlass = true,
        shape = AstraShapes.ModalSheet,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Android System Widgets",
                        style = AstraTypography.TitleL,
                        color = palette.primaryText
                    )
                    Text(
                        text = "${providers.size} installed widget providers discovered via AppWidgetManager",
                        style = AstraTypography.Caption,
                        color = palette.secondaryText
                    )
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onDismiss),
                    shape = AstraShapes.ChipPill,
                    color = palette.elevatedSurface,
                    border = BorderStroke(1.dp, palette.hairlineBorder)
                ) {
                    Text(
                        text = "Close",
                        style = AstraTypography.Caption,
                        color = palette.primaryText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (providers.isEmpty()) {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No AppWidget providers reported by Android on this profile",
                            style = AstraTypography.SectionHeader,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Install apps that export AppWidgetProvider receivers to host them on your workspace.",
                            style = AstraTypography.Caption,
                            color = palette.secondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(providers, key = { it.providerComponent.flattenToString() }) { provider ->
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = false,
                            onClick = { onSelectProvider(provider) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = provider.label,
                                        style = AstraTypography.SectionHeader,
                                        color = palette.primaryText
                                    )
                                    Text(
                                        text = "${provider.packageName} · ${provider.recommendedSpanX}×${provider.recommendedSpanY} grid (${provider.minWidthDp}×${provider.minHeightDp}dp)",
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                    if (provider.hasConfigurationActivity) {
                                        Text(
                                            text = "Includes widget configuration activity",
                                            style = AstraTypography.Caption,
                                            color = palette.primaryAccent
                                        )
                                    }
                                }
                                Surface(
                                    shape = AstraShapes.ChipPill,
                                    color = palette.primaryAccent
                                ) {
                                    Text(
                                        text = "+ Add",
                                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.obsidian0,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
