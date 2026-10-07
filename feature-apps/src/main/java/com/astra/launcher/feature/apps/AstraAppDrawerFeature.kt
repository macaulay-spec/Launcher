package com.astra.launcher.feature.apps

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
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraSearchField
import com.astra.launcher.core.design.AstraSegmentedControl
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.storage.AppCategory
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle

enum class AppDrawerMode(val label: String) {
    ALL("All Apps"),
    CATEGORIES("Categories"),
    FAVORITES("Favorites"),
    PRIVATE("Hidden")
}

/**
 * App Drawer / App Library (Section 14, Frame 08).
 * Fast, visually calm atmospheric surface over the wallpaper with Recent, Suggested,
 * Alphabetical fast rail, Intelligent Categories, Favorites, and Private/Hidden apps.
 */
@Composable
fun AstraAppDrawerScreen(
    apps: List<AstraAppEntry>,
    favoritePackages: Set<String>,
    hiddenPackages: Set<String>,
    privateUnlocked: Boolean,
    iconStyle: AstraIconStyle,
    iconScale: Float,
    showLabels: Boolean,
    onLaunchApp: (AstraAppEntry) -> Unit,
    onLongPressApp: (AstraAppEntry) -> Unit,
    onTogglePrivateLock: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var filterQuery by remember { mutableStateOf("") }
    var drawerMode by remember { mutableStateOf(AppDrawerMode.ALL) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    val visibleApps = remember(apps, hiddenPackages, filterQuery, selectedLetter) {
        apps.filter { app ->
            val notHidden = app.packageName !in hiddenPackages
            val matchesText = filterQuery.isBlank() ||
                app.label.contains(filterQuery, ignoreCase = true) ||
                app.aliases.any { it.contains(filterQuery, ignoreCase = true) }
            val matchesLetter = selectedLetter == null ||
                app.label.firstOrNull()?.uppercaseChar() == selectedLetter
            notHidden && matchesText && matchesLetter
        }
    }

    val suggestedApps = remember(visibleApps) {
        visibleApps.sortedByDescending { it.usageScore }.take(4)
    }

    val alphabetLetters = remember(apps) {
        apps.mapNotNull { it.label.firstOrNull()?.uppercaseChar() }.distinct().sorted()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ASTRA APP LIBRARY",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary
                    )
                    Text(
                        text = "Applications & Spaces",
                        style = AstraTheme.typography.titleL,
                        color = colors.textPrimary
                    )
                }
                AstraIconButton(
                    glyph = AstraGlyph.CLOSE,
                    contentDescriptionLabel = "Close App Library",
                    onClick = onClose
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AstraSearchField(
                query = filterQuery,
                onQueryChange = {
                    filterQuery = it
                    selectedLetter = null
                },
                placeholder = "Filter ${apps.size} installed apps...",
                onClear = { filterQuery = "" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AstraSegmentedControl(
                items = AppDrawerMode.entries,
                selectedItem = drawerMode,
                labelProvider = { it.label },
                onSelect = {
                    drawerMode = it
                    selectedLetter = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (drawerMode) {
                AppDrawerMode.ALL -> {
                    if (filterQuery.isBlank() && selectedLetter == null) {
                        Text(
                            text = "SUGGESTED & RECENT",
                            style = AstraTheme.typography.labelS,
                            color = colors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            suggestedApps.forEach { app ->
                                AstraAppIcon(
                                    app = app,
                                    iconStyle = iconStyle,
                                    iconScale = iconScale,
                                    showLabel = showLabels,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedLetter != null) "LETTER '$selectedLetter'" else "ALL APPS (${visibleApps.size})",
                            style = AstraTheme.typography.labelS,
                            color = colors.textTertiary
                        )
                        if (selectedLetter != null) {
                            Text(
                                text = "Clear Filter",
                                style = AstraTheme.typography.labelS,
                                color = colors.accentPrimary,
                                modifier = Modifier.clickable { selectedLetter = null }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    visibleApps.chunked(4).forEach { rowApps ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            for (i in 0 until 4) {
                                val app = rowApps.getOrNull(i)
                                if (app != null) {
                                    AstraAppIcon(
                                        app = app,
                                        iconStyle = iconStyle,
                                        iconScale = iconScale,
                                        showLabel = showLabels,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onLongPressApp(app) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                AppDrawerMode.CATEGORIES -> {
                    AppCategory.entries.forEach { cat ->
                        val catApps = visibleApps.filter { it.category == cat }
                        if (catApps.isNotEmpty()) {
                            AstraCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                family = AstraSurfaceFamily.CLEAR_ATMOSPHERIC
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat.label.uppercase(),
                                            style = AstraTheme.typography.labelS,
                                            color = colors.accentPrimary
                                        )
                                        Text(
                                            text = "${catApps.size} apps",
                                            style = AstraTheme.typography.labelS,
                                            color = colors.textTertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    catApps.chunked(4).forEach { row ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            for (i in 0 until 4) {
                                                val item = row.getOrNull(i)
                                                if (item != null) {
                                                    AstraAppIcon(
                                                        app = item,
                                                        iconStyle = iconStyle,
                                                        iconScale = iconScale * 0.94f,
                                                        showLabel = showLabels,
                                                        onClick = { onLaunchApp(item) },
                                                        onLongClick = { onLongPressApp(item) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AppDrawerMode.FAVORITES -> {
                    val favApps = visibleApps.filter { it.packageName in favoritePackages || it.usageScore >= 85 }
                    Text(
                        text = "PRIORITY & FAVORITES (${favApps.size})",
                        style = AstraTheme.typography.labelS,
                        color = colors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    favApps.chunked(4).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (i in 0 until 4) {
                                val item = row.getOrNull(i)
                                if (item != null) {
                                    AstraAppIcon(
                                        app = item,
                                        iconStyle = iconStyle,
                                        iconScale = iconScale,
                                        showLabel = showLabels,
                                        onClick = { onLaunchApp(item) },
                                        onLongClick = { onLongPressApp(item) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                AppDrawerMode.PRIVATE -> {
                    val hiddenList = apps.filter { it.packageName in hiddenPackages }
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PRIVATE & HIDDEN SPACE",
                                        style = AstraTheme.typography.labelS,
                                        color = colors.accentPrimary
                                    )
                                    Text(
                                        text = if (privateUnlocked) "Vault Unlocked" else "Protected by Device Biometrics / PIN",
                                        style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                }
                                AstraIconButton(
                                    glyph = if (privateUnlocked) AstraGlyph.UNLOCK else AstraGlyph.LOCK,
                                    contentDescriptionLabel = "Toggle Vault Lock",
                                    onClick = onTogglePrivateLock
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (!privateUnlocked) {
                                Text(
                                    text = "Tap the lock control to authenticate and view ${hiddenList.size} hidden applications. Long-press any app in All Apps to hide or restore it.",
                                    style = AstraTheme.typography.bodyS,
                                    color = colors.textSecondary
                                )
                            } else if (hiddenList.isEmpty()) {
                                Text(
                                    text = "No apps are currently hidden. Long-press any app in the library and select 'Hide from Home & Library'.",
                                    style = AstraTheme.typography.bodyS,
                                    color = colors.textSecondary
                                )
                            } else {
                                hiddenList.chunked(4).forEach { row ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        for (i in 0 until 4) {
                                            val item = row.getOrNull(i)
                                            if (item != null) {
                                                AstraAppIcon(
                                                    app = item,
                                                    iconStyle = iconStyle,
                                                    onClick = { onLaunchApp(item) },
                                                    onLongClick = { onLongPressApp(item) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
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

        // Fast Alphabet Index Rail (Section 14)
        if (drawerMode == AppDrawerMode.ALL) {
            Spacer(modifier = Modifier.width(6.dp))
            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surfaceGlass)
                    .border(1.dp, colors.borderSubtle, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                alphabetLetters.forEach { letter ->
                    val isSelected = selectedLetter == letter
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) colors.accentPrimary else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable {
                                selectedLetter = if (selectedLetter == letter) null else letter
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            style = AstraTheme.typography.labelS,
                            color = if (isSelected) colors.surfaceBase else colors.textTertiary
                        )
                    }
                }
            }
        }
    }
}
