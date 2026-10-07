package com.astra.launcher.feature.apps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraCategoryChip
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.storage.AppCategory
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle
import kotlinx.coroutines.launch
import java.util.Locale

enum class DrawerFilterTab(val id: String, val label: String) {
    ALL("all", "All Apps"),
    WORK("work", "Work Profile"),
    FAVORITES("favorites", "Favorites"),
    COMMUNICATION("comm", "Communication"),
    MEDIA("media", "Media"),
    PRODUCTIVITY("prod", "Productivity"),
    UTILITIES("util", "Utilities"),
    HIDDEN("hidden", "Hidden")
}

/**
 * Real Android App Drawer / App Library Overlay (Section 10).
 * Sits over the persistent Home Workspace and lists all real installed launchable activities.
 */
@Composable
fun AstraAppDrawerOverlay(
    installedApps: List<AstraAppEntry>,
    hiddenComponents: Set<String>,
    favoriteComponents: Set<String>,
    showCategories: Boolean,
    showRecentRow: Boolean,
    iconStyle: AstraIconStyle,
    iconScale: Float,
    showLabels: Boolean,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline?,
    onAppClick: (AstraAppEntry) -> Unit,
    onAppLongClick: (AstraAppEntry) -> Unit,
    onRescanPackages: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(DrawerFilterTab.ALL) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val hasWorkApps = remember(installedApps) { installedApps.any { it.isWorkProfile } }

    val availableTabs = remember(showCategories, hasWorkApps, hiddenComponents) {
        buildList {
            add(DrawerFilterTab.ALL)
            if (hasWorkApps) add(DrawerFilterTab.WORK)
            add(DrawerFilterTab.FAVORITES)
            if (showCategories) {
                add(DrawerFilterTab.COMMUNICATION)
                add(DrawerFilterTab.MEDIA)
                add(DrawerFilterTab.PRODUCTIVITY)
                add(DrawerFilterTab.UTILITIES)
            }
            add(DrawerFilterTab.HIDDEN)
        }
    }

    val filteredApps = remember(installedApps, hiddenComponents, favoriteComponents, selectedTab, searchQuery) {
        val base = when (selectedTab) {
            DrawerFilterTab.HIDDEN -> installedApps.filter { it.componentName in hiddenComponents }
            DrawerFilterTab.WORK -> installedApps.filter { it.isWorkProfile && it.componentName !in hiddenComponents }
            DrawerFilterTab.FAVORITES -> installedApps.filter { it.componentName in favoriteComponents && it.componentName !in hiddenComponents }
            DrawerFilterTab.COMMUNICATION -> installedApps.filter { it.category == AppCategory.COMMUNICATION && it.componentName !in hiddenComponents }
            DrawerFilterTab.MEDIA -> installedApps.filter { it.category == AppCategory.MEDIA && it.componentName !in hiddenComponents }
            DrawerFilterTab.PRODUCTIVITY -> installedApps.filter { it.category == AppCategory.PRODUCTIVITY && it.componentName !in hiddenComponents }
            DrawerFilterTab.UTILITIES -> installedApps.filter { it.category == AppCategory.UTILITIES && it.componentName !in hiddenComponents }
            DrawerFilterTab.ALL -> installedApps.filterNot { it.componentName in hiddenComponents }
        }

        val q = searchQuery.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) {
            base.sortedBy { it.label.lowercase(Locale.getDefault()) }
        } else {
            base.filter {
                it.label.lowercase(Locale.getDefault()).contains(q) ||
                    it.packageName.lowercase(Locale.getDefault()).contains(q)
            }.sortedBy { it.label.lowercase(Locale.getDefault()) }
        }
    }

    val recentApps = remember(installedApps, hiddenComponents) {
        installedApps
            .filterNot { it.componentName in hiddenComponents }
            .sortedWith(
                compareByDescending<AstraAppEntry> { it.lastUsedTimestamp }
                    .thenByDescending { it.usageScore }
                    .thenBy { it.label.lowercase(Locale.getDefault()) }
            )
            .take(5)
    }

    val alphabetIndexMap = remember(filteredApps) {
        val map = mutableMapOf<Char, Int>()
        filteredApps.forEachIndexed { idx, app ->
            val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
            val key = if (firstChar in 'A'..'Z') firstChar else '#'
            if (!map.containsKey(key)) {
                map[key] = idx
            }
        }
        map
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Drawer Top Handle + Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Library",
                        style = AstraTypography.TitleL,
                        color = palette.primaryText
                    )
                    Text(
                        text = "${installedApps.size} installed apps · Long-press any app for shortcuts or to pin to Home",
                        style = AstraTypography.Caption,
                        color = palette.secondaryText
                    )
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onCloseDrawer),
                    shape = AstraShapes.ChipPill,
                    color = palette.elevatedSurface,
                    border = BorderStroke(1.dp, palette.hairlineBorder)
                ) {
                    Text(
                        text = "Home ↓",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primaryAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // In-Drawer Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = AstraShapes.SearchPill,
                color = palette.elevatedSurface,
                border = BorderStroke(1.dp, palette.glassStroke)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AstraVectorIcon(
                        glyph = AstraGlyph.SEARCH,
                        tint = palette.primaryAccent,
                        size = 18.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Filter ${installedApps.size} installed apps…",
                                style = AstraTypography.BodyM,
                                color = palette.mutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = AstraTypography.BodyM.copy(color = palette.primaryText),
                            cursorBrush = SolidColor(palette.primaryAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            AstraVectorIcon(
                                glyph = AstraGlyph.CLOSE,
                                tint = palette.secondaryText,
                                size = 14.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(availableTabs, key = { it.id }) { tab ->
                    AstraCategoryChip(
                        label = tab.label,
                        selected = selectedTab == tab,
                        palette = palette,
                        onClick = { selectedTab = tab }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Alphabetical Grid + Right Fast-Scroll A-Z Rail
            Row(modifier = Modifier.weight(1f)) {
                if (installedApps.isEmpty()) {
                    // Honest Recovery State when 0 packages are returned (Section 31)
                    AstraSurfaceCard(
                        palette = palette,
                        useGlass = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "No Launchable Applications Discovered",
                                style = AstraTypography.SectionHeader,
                                color = palette.primaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Astra queries Android's LauncherApps and PackageManager for real installed apps and never injects fake demo apps.",
                                style = AstraTypography.BodyM,
                                color = palette.secondaryText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.clickable(onClick = onRescanPackages),
                                shape = AstraShapes.ChipPill,
                                color = palette.primaryAccent
                            ) {
                                Text(
                                    text = "Retry Package Scan",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.obsidian0,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        state = gridState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 28.dp)
                    ) {
                        // Optional Recent/Frequent Row
                        if (showRecentRow && selectedTab == DrawerFilterTab.ALL && searchQuery.isBlank() && recentApps.isNotEmpty()) {
                            item(span = { GridItemSpan(4) }, key = "recent_header") {
                                Text(
                                    text = "RECENT & FREQUENT",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.secondaryText
                                )
                            }
                            items(recentApps.take(4), key = { "recent_${it.componentName}" }) { app ->
                                AstraAppIcon(
                                    app = app,
                                    iconStyle = iconStyle,
                                    palette = palette,
                                    iconPipeline = iconPipeline,
                                    showLabel = showLabels,
                                    iconScale = iconScale,
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) }
                                )
                            }
                            item(span = { GridItemSpan(4) }, key = "all_apps_divider") {
                                Text(
                                    text = "ALL INSTALLED (${filteredApps.size})",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.secondaryText,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }

                        items(filteredApps, key = { "${it.componentName}_${it.userSerial}" }) { app ->
                            AstraAppIcon(
                                app = app,
                                iconStyle = iconStyle,
                                palette = palette,
                                iconPipeline = iconPipeline,
                                showLabel = showLabels,
                                iconScale = iconScale,
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                        }
                    }

                    // A–Z Fast-Scroll Index Rail
                    if (alphabetIndexMap.size > 1) {
                        Column(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ('A'..'Z').forEach { letter ->
                                val targetIdx = alphabetIndexMap[letter]
                                Text(
                                    text = letter.toString(),
                                    style = AstraTypography.Caption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (targetIdx != null) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (targetIdx != null) palette.primaryAccent else palette.mutedText.copy(alpha = 0.45f),
                                    modifier = Modifier.clickable(enabled = targetIdx != null) {
                                        if (targetIdx != null) {
                                            coroutineScope.launch {
                                                gridState.animateScrollToItem(targetIdx)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
