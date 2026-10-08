package com.astra.launcher.feature.apps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.astra.launcher.core.storage.AppSortOrder
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale

enum class DiscoveryViewMode(val id: String, val label: String) {
    DISCOVERY("discovery", "Discovery"),
    ALL_APPS("all_apps", "All Apps"),
    HIDDEN("hidden", "Hidden")
}

/**
 * Modern Application Discovery Surface (`AstraAppDrawerOverlay` — Rebuild Sections 4 & 27).
 *
 * Replaces primitive A–Z-only app grids with an intelligent discovery surface:
 * Top Search -> Recently Used -> Favorites -> Contextual Suggestions -> Smart Categories -> All Apps
 * (with optional Usage vs A–Z sorting and A–Z index rail).
 */
@OptIn(ExperimentalLayoutApi::class)
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
    var viewMode by remember {
        mutableStateOf(if (showCategories) DiscoveryViewMode.DISCOVERY else DiscoveryViewMode.ALL_APPS)
    }
    var sortOrder by remember { mutableStateOf(AppSortOrder.MOST_USED) }
    var selectedCategoryFilter by remember { mutableStateOf<AppCategory?>(null) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val visibleApps = remember(installedApps, hiddenComponents) {
        installedApps.filterNot { it.componentName in hiddenComponents }
    }

    val hiddenApps = remember(installedApps, hiddenComponents) {
        installedApps.filter { it.componentName in hiddenComponents }
    }

    val recentlyUsedApps = remember(visibleApps) {
        visibleApps
            .filter { it.lastUsedTimestamp > 0L || it.usageScore > 0 }
            .sortedWith(
                compareByDescending<AstraAppEntry> { it.lastUsedTimestamp }
                    .thenByDescending { it.usageScore }
            )
            .take(6)
    }

    val favoriteApps = remember(visibleApps, favoriteComponents) {
        val explicitFavs = visibleApps.filter { it.componentName in favoriteComponents }
        if (explicitFavs.isNotEmpty()) {
            explicitFavs.take(8)
        } else {
            visibleApps
                .sortedWith(
                    compareByDescending<AstraAppEntry> { it.usageScore }
                        .thenBy { it.label.lowercase(Locale.getDefault()) }
                )
                .take(6)
        }
    }

    // Contextual Suggestions based on real time of day + usage frequency (Rebuild Section 4 & 21)
    val contextualSuggestedApps = remember(visibleApps) {
        val currentHour = try {
            LocalTime.now().hour
        } catch (_: Throwable) {
            12
        }
        val preferredCategories = when (currentHour) {
            in 5..10 -> setOf(AppCategory.PRODUCTIVITY, AppCategory.COMMUNICATION, AppCategory.INTERNET)
            in 11..17 -> setOf(AppCategory.PRODUCTIVITY, AppCategory.UTILITIES, AppCategory.COMMUNICATION)
            else -> setOf(AppCategory.MEDIA, AppCategory.COMMUNICATION, AppCategory.GAMES_OTHER)
        }
        visibleApps
            .sortedWith(
                compareByDescending<AstraAppEntry> { it.category in preferredCategories }
                    .thenByDescending { it.usageScore }
                    .thenBy { it.label.lowercase(Locale.getDefault()) }
            )
            .take(4)
    }

    val categorizedGroups = remember(visibleApps) {
        AppCategory.entries.mapNotNull { cat ->
            val members = visibleApps.filter { it.category == cat }
            if (members.isNotEmpty()) cat to members else null
        }
    }

    val filteredAllApps = remember(visibleApps, hiddenApps, viewMode, sortOrder, selectedCategoryFilter, searchQuery) {
        val base = when (viewMode) {
            DiscoveryViewMode.HIDDEN -> hiddenApps
            else -> {
                if (selectedCategoryFilter != null) {
                    visibleApps.filter { it.category == selectedCategoryFilter }
                } else {
                    visibleApps
                }
            }
        }
        val q = searchQuery.trim().lowercase(Locale.getDefault())
        val matched = if (q.isEmpty()) {
            base
        } else {
            base.filter {
                it.label.lowercase(Locale.getDefault()).contains(q) ||
                    it.packageName.lowercase(Locale.getDefault()).contains(q)
            }
        }
        when (sortOrder) {
            AppSortOrder.MOST_USED -> matched.sortedWith(
                compareByDescending<AstraAppEntry> { it.usageScore }
                    .thenByDescending { it.lastUsedTimestamp }
                    .thenBy { it.label.lowercase(Locale.getDefault()) }
            )
            AppSortOrder.ALPHABETICAL -> matched.sortedBy { it.label.lowercase(Locale.getDefault()) }
        }
    }

    val alphabetIndexMap = remember(filteredAllApps) {
        val map = mutableMapOf<Char, Int>()
        filteredAllApps.forEachIndexed { idx, app ->
            val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
            val key = if (firstChar in 'A'..'Z') firstChar else '#'
            if (!map.containsKey(key)) map[key] = idx
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
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Discovery",
                        style = AstraTypography.TitleL,
                        color = palette.primaryText
                    )
                    Text(
                        text = "${visibleApps.size} apps · Favorites, Recent, Categories & Fast Search",
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

            // Top Search Bar (Rebuild Section 27)
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
                                text = "Search applications, categories, or packages…",
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

            // Mode & Sort Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiscoveryViewMode.entries.forEach { mode ->
                        if (mode != DiscoveryViewMode.HIDDEN || hiddenApps.isNotEmpty()) {
                            AstraCategoryChip(
                                label = mode.label,
                                selected = viewMode == mode && selectedCategoryFilter == null,
                                palette = palette,
                                onClick = {
                                    viewMode = mode
                                    selectedCategoryFilter = null
                                }
                            )
                        }
                    }
                }

                if (viewMode == DiscoveryViewMode.ALL_APPS || searchQuery.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppSortOrder.entries.forEach { order ->
                            AstraCategoryChip(
                                label = order.label,
                                selected = sortOrder == order,
                                palette = palette,
                                onClick = { sortOrder = order }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (installedApps.isEmpty()) {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "No Installed Applications Found",
                            style = AstraTypography.SectionHeader,
                            color = palette.primaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Astra discovers real applications installed on your device via Android's LauncherApps service.",
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
                                text = "Rescan Installed Packages",
                                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.obsidian0,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else if (viewMode == DiscoveryViewMode.DISCOVERY && searchQuery.isBlank() && selectedCategoryFilter == null) {
                // MODERN DISCOVERY SURFACE: Recently Used -> Favorites -> Contextual Suggestions -> Smart Categories -> All Apps
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    // 1. Recently Used
                    if (showRecentRow && recentlyUsedApps.isNotEmpty()) {
                        item(key = "sec_recent") {
                            DiscoveryHorizontalSection(
                                title = "RECENTLY USED",
                                subtitle = "Quick return to your active tasks",
                                apps = recentlyUsedApps,
                                iconStyle = iconStyle,
                                iconScale = iconScale,
                                showLabels = showLabels,
                                palette = palette,
                                iconPipeline = iconPipeline,
                                onAppClick = onAppClick,
                                onAppLongClick = onAppLongClick
                            )
                        }
                    }

                    // 2. Favorites & Most Used
                    if (favoriteApps.isNotEmpty()) {
                        item(key = "sec_favorites") {
                            DiscoveryHorizontalSection(
                                title = "FAVORITES & FREQUENT",
                                subtitle = "Your core daily applications",
                                apps = favoriteApps,
                                iconStyle = iconStyle,
                                iconScale = iconScale,
                                showLabels = showLabels,
                                palette = palette,
                                iconPipeline = iconPipeline,
                                onAppClick = onAppClick,
                                onAppLongClick = onAppLongClick
                            )
                        }
                    }

                    // 3. Contextual Suggestions
                    if (contextualSuggestedApps.isNotEmpty()) {
                        item(key = "sec_suggested") {
                            AstraSurfaceCard(
                                palette = palette,
                                useGlass = true,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SUGGESTED FOR NOW",
                                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                            color = palette.primaryAccent
                                        )
                                        Text(
                                            text = "Contextual",
                                            style = AstraTypography.Caption,
                                            color = palette.secondaryText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        contextualSuggestedApps.forEach { app ->
                                            AstraAppIcon(
                                                app = app,
                                                iconStyle = iconStyle,
                                                palette = palette,
                                                iconPipeline = iconPipeline,
                                                showLabel = showLabels,
                                                iconScale = iconScale,
                                                onClick = { onAppClick(app) },
                                                onLongClick = { onAppLongClick(app) },
                                                modifier = Modifier.width(68.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Smart Categories Bento Grid (Rebuild Section 4 & 27)
                    if (categorizedGroups.isNotEmpty()) {
                        item(key = "sec_categories_header") {
                            Text(
                                text = "CATEGORIES",
                                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.secondaryText
                            )
                        }
                        items(categorizedGroups, key = { it.first.id }) { (category, appsInCategory) ->
                            AstraSurfaceCard(
                                palette = palette,
                                useGlass = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category.label,
                                            style = AstraTypography.SectionHeader,
                                            color = palette.primaryText
                                        )
                                        Text(
                                            text = "View all (${appsInCategory.size}) →",
                                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                            color = palette.primaryAccent,
                                            modifier = Modifier.clickable {
                                                selectedCategoryFilter = category
                                                viewMode = DiscoveryViewMode.ALL_APPS
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        maxItemsInEachRow = 4
                                    ) {
                                        appsInCategory.take(8).forEach { app ->
                                            AstraAppIcon(
                                                app = app,
                                                iconStyle = iconStyle,
                                                palette = palette,
                                                iconPipeline = iconPipeline,
                                                showLabel = showLabels,
                                                iconScale = iconScale * 0.94f,
                                                onClick = { onAppClick(app) },
                                                onLongClick = { onAppLongClick(app) },
                                                modifier = Modifier.width(68.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Quick switch to All Applications
                    item(key = "sec_all_apps_cta") {
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = true,
                            onClick = { viewMode = DiscoveryViewMode.ALL_APPS },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Browse All ${visibleApps.size} Installed Applications",
                                        style = AstraTypography.SectionHeader,
                                        color = palette.primaryText
                                    )
                                    Text(
                                        text = "Sort by Most Used or Alphabetical index",
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = "Open →",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryAccent
                                )
                            }
                        }
                    }
                }
            } else {
                // ALL APPS / FILTERED / SEARCH RESULTS VIEW (with Usage or A–Z sort + optional A–Z index rail)
                if (selectedCategoryFilter != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category: ${selectedCategoryFilter!!.label} (${filteredAllApps.size})",
                            style = AstraTypography.SectionHeader,
                            color = palette.primaryAccent
                        )
                        Text(
                            text = "Back to Discovery",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.primaryText,
                            modifier = Modifier.clickable {
                                selectedCategoryFilter = null
                                viewMode = DiscoveryViewMode.DISCOVERY
                            }
                        )
                    }
                }

                Row(modifier = Modifier.weight(1f)) {
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
                        items(filteredAllApps, key = { "${it.componentName}_${it.userSerial}" }) { app ->
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

                    // Optional Alphabetical Index Rail when sorted A–Z
                    if (sortOrder == AppSortOrder.ALPHABETICAL && alphabetIndexMap.size > 1) {
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

@Composable
private fun DiscoveryHorizontalSection(
    title: String,
    subtitle: String,
    apps: List<AstraAppEntry>,
    iconStyle: AstraIconStyle,
    iconScale: Float,
    showLabels: Boolean,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline?,
    onAppClick: (AstraAppEntry) -> Unit,
    onAppLongClick: (AstraAppEntry) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                color = palette.secondaryText
            )
            Text(
                text = subtitle,
                style = AstraTypography.Caption,
                color = palette.mutedText
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(apps, key = { "disc_${title}_${it.componentName}" }) { app ->
                AstraAppIcon(
                    app = app,
                    iconStyle = iconStyle,
                    palette = palette,
                    iconPipeline = iconPipeline,
                    showLabel = showLabels,
                    iconScale = iconScale,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.width(68.dp)
                )
            }
        }
    }
}
