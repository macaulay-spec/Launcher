package com.astra.launcher.feature.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraShortcutItem
import com.astra.launcher.core.storage.SearchPreferences
import java.util.Locale
import kotlin.math.roundToLong

enum class SearchSettingAction(val id: String, val title: String, val subtitle: String, val keywords: String) {
    WIFI("wifi", "Wi-Fi & Internet Panel", "Open Android connectivity panel", "wifi internet network wlan"),
    BLUETOOTH("bluetooth", "Bluetooth Settings", "Manage paired audio and accessories", "bluetooth bt headphones pair"),
    DISPLAY("display", "Display & Brightness", "Open Android display settings", "display brightness screen dark mode"),
    SOUND("sound", "Sound & Vibration", "Volume, ringtone, Do Not Disturb", "sound volume audio vibration ring"),
    WALLPAPER("wallpaper", "Astra Personalization & Wallpaper", "Configure wallpaper, theme, clock, icons", "wallpaper theme style personalize"),
    DEFAULT_HOME("default_home", "Default Home App Role", "Set or change Android default launcher", "default home role launcher"),
    NOTIFICATION_SHADE("notif_shade", "Expand Android Notification Shade", "Pull down system status bar notifications", "notifications shade status bar"),
    SYSTEM_SETTINGS("system_settings", "Android System Settings", "Open full device settings", "settings system android phone")
}

enum class SearchCommandAction(val id: String, val title: String, val subtitle: String, val keywords: String) {
    OPEN_APP_DRAWER("open_drawer", "Open App Drawer", "Browse all installed applications", "drawer apps library all"),
    OPEN_WIDGET_PICKER("open_widgets", "Add Android Widget to Home", "Open AppWidgetManager provider picker", "widget widgets add host"),
    EDIT_HOME_WORKSPACE("edit_home", "Enter Home Workspace Edit Mode", "Move icons, resize widgets, manage pages", "edit workspace grid pages arrange"),
    OPEN_ASTRA_SETTINGS("astra_settings", "Astra Launcher Settings", "Configure 14 launcher settings categories", "astra launcher settings config"),
    REFRESH_PACKAGES("refresh_packages", "Rescan Installed Packages", "Force LauncherApps & icon cache refresh", "refresh rescan packages icons reload")
}

data class SearchCalculationResult(
    val expression: String,
    val formattedValue: String,
    val detailLabel: String
)

data class SearchQueryBundle(
    val query: String,
    val matchedApps: List<AstraAppEntry>,
    val matchedShortcuts: List<Pair<AstraAppEntry, AstraShortcutItem>>,
    val matchedSettings: List<SearchSettingAction>,
    val matchedCommands: List<SearchCommandAction>,
    val calculation: SearchCalculationResult?
) {
    val isEmpty: Boolean
        get() = matchedApps.isEmpty() &&
            matchedShortcuts.isEmpty() &&
            matchedSettings.isEmpty() &&
            matchedCommands.isEmpty() &&
            calculation == null
}

/**
 * Real Launcher Search Index (Section 11).
 * Queries actual installed apps, real LauncherApps shortcuts, honest system settings deep-links,
 * and deterministic local math/unit evaluation.
 */
object AstraSearchIndex {

    fun query(
        rawQuery: String,
        installedApps: List<AstraAppEntry>,
        hiddenComponents: Set<String>,
        preferences: SearchPreferences
    ): SearchQueryBundle {
        val visibleApps = installedApps.filterNot { it.componentName in hiddenComponents }
        val q = rawQuery.trim()
        val lower = q.lowercase(Locale.getDefault())

        if (q.isEmpty()) {
            // Zero-query state: show recent/frequent real installed apps + quick launcher commands
            val topApps = visibleApps
                .sortedWith(
                    compareByDescending<AstraAppEntry> { it.usageScore }
                        .thenByDescending { it.lastUsedTimestamp }
                        .thenBy { it.label.lowercase(Locale.getDefault()) }
                )
                .take(8)
            return SearchQueryBundle(
                query = "",
                matchedApps = topApps,
                matchedShortcuts = emptyList(),
                matchedSettings = listOf(
                    SearchSettingAction.WIFI,
                    SearchSettingAction.BLUETOOTH,
                    SearchSettingAction.DISPLAY,
                    SearchSettingAction.WALLPAPER
                ),
                matchedCommands = SearchCommandAction.entries,
                calculation = null
            )
        }

        val apps = if (preferences.searchApps) {
            visibleApps
                .mapNotNull { app ->
                    val score = scoreAppMatch(app, lower)
                    if (score > 0) app to score else null
                }
                .sortedWith(
                    compareByDescending<Pair<AstraAppEntry, Int>> { it.second }
                        .thenByDescending { it.first.usageScore }
                        .thenBy { it.first.label.lowercase(Locale.getDefault()) }
                )
                .map { it.first }
                .take(12)
        } else emptyList()

        val shortcuts = if (preferences.searchShortcuts) {
            visibleApps.flatMap { app ->
                app.shortcuts.filter { s ->
                    s.shortLabel.lowercase(Locale.getDefault()).contains(lower) ||
                        s.longLabel.lowercase(Locale.getDefault()).contains(lower) ||
                        app.label.lowercase(Locale.getDefault()).startsWith(lower)
                }.map { app to it }
            }.take(6)
        } else emptyList()

        val settings = if (preferences.searchSettings) {
            SearchSettingAction.entries.filter {
                it.title.lowercase(Locale.getDefault()).contains(lower) ||
                    it.subtitle.lowercase(Locale.getDefault()).contains(lower) ||
                    it.keywords.contains(lower)
            }
        } else emptyList()

        val commands = if (preferences.searchCommands) {
            SearchCommandAction.entries.filter {
                it.title.lowercase(Locale.getDefault()).contains(lower) ||
                    it.subtitle.lowercase(Locale.getDefault()).contains(lower) ||
                    it.keywords.contains(lower)
            }
        } else emptyList()

        val calc = evaluateExpressionOrConversion(lower)

        return SearchQueryBundle(
            query = q,
            matchedApps = apps,
            matchedShortcuts = shortcuts,
            matchedSettings = settings,
            matchedCommands = commands,
            calculation = calc
        )
    }

    private fun scoreAppMatch(app: AstraAppEntry, queryLower: String): Int {
        val labelLower = app.label.lowercase(Locale.getDefault())
        val pkgLower = app.packageName.lowercase(Locale.getDefault())
        return when {
            labelLower == queryLower -> 100
            labelLower.startsWith(queryLower) -> 85
            labelLower.split(" ").any { it.startsWith(queryLower) } -> 75
            labelLower.contains(queryLower) -> 55
            pkgLower.contains(queryLower) -> 35
            matchesAcronym(labelLower, queryLower) -> 45
            else -> 0
        }
    }

    private fun matchesAcronym(labelLower: String, queryLower: String): Boolean {
        if (queryLower.length < 2) return false
        val initials = labelLower.split(" ", "-", "_").mapNotNull { it.firstOrNull() }.joinToString("")
        return initials.startsWith(queryLower)
    }

    fun evaluateExpressionOrConversion(input: String): SearchCalculationResult? {
        val trimmed = input.trim()
        // Unit conversion: e.g., "10 km to mi", "72 f to c", "5 kg to lb"
        val unitRegex = Regex("""^(-?\d+(?:\.\d+)?)\s*([a-zA-Z°]+)\s+(?:to|in)\s+([a-zA-Z°]+)$""")
        unitRegex.matchEntire(trimmed)?.let { match ->
            val value = match.groupValues[1].toDoubleOrNull() ?: return null
            val from = match.groupValues[2].lowercase(Locale.getDefault())
            val to = match.groupValues[3].lowercase(Locale.getDefault())
            val converted = when (from to to) {
                "km" to "mi" -> (value * 0.621371) to "mi"
                "mi" to "km" -> (value / 0.621371) to "km"
                "kg" to "lb", "kg" to "lbs" -> (value * 2.20462) to "lb"
                "lb" to "kg", "lbs" to "kg" -> (value / 2.20462) to "kg"
                "c" to "f", "°c" to "°f" -> ((value * 9.0 / 5.0) + 32.0) to "°F"
                "f" to "c", "°f" to "°c" -> ((value - 32.0) * 5.0 / 9.0) to "°C"
                "m" to "ft" -> (value * 3.28084) to "ft"
                "ft" to "m" -> (value / 3.28084) to "m"
                else -> null
            } ?: return null

            val formatted = String.format(Locale.US, "%.2f %s", converted.first, converted.second)
            return SearchCalculationResult(
                expression = trimmed,
                formattedValue = formatted,
                detailLabel = "Instant Unit Conversion"
            )
        }

        // Deterministic arithmetic: a (+|-|*|/) b
        val mathRegex = Regex("""^\s*(-?\d+(?:\.\d+)?)\s*([+\-*/x×÷])\s*(-?\d+(?:\.\d+)?)\s*$""")
        mathRegex.matchEntire(trimmed)?.let { match ->
            val a = match.groupValues[1].toDoubleOrNull() ?: return null
            val op = match.groupValues[2]
            val b = match.groupValues[3].toDoubleOrNull() ?: return null
            val res = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*", "x", "×" -> a * b
                "/", "÷" -> if (b != 0.0) a / b else return null
                else -> return null
            }
            val formatted = if (res == res.roundToLong().toDouble()) {
                res.roundToLong().toString()
            } else {
                String.format(Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
            }
            return SearchCalculationResult(
                expression = "$a $op $b",
                formattedValue = formatted,
                detailLabel = "Instant Calculation"
            )
        }
        return null
    }
}

/**
 * Keyboard-First Launcher Search Overlay (Section 11 & 12).
 * Sits over the persistent Home Workspace with immediate software/hardware keyboard focus,
 * Enter-to-launch top result, Escape-to-close, arrow-key navigation, and IME-safe padding.
 */
@Composable
fun AstraSearchOverlay(
    installedApps: List<AstraAppEntry>,
    hiddenComponents: Set<String>,
    searchPreferences: SearchPreferences,
    iconStyle: AstraIconStyle,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline?,
    onLaunchApp: (AstraAppEntry) -> Unit,
    onAppLongPress: (AstraAppEntry) -> Unit,
    onLaunchShortcut: (AstraShortcutItem) -> Unit,
    onExecuteSettingAction: (SearchSettingAction) -> Unit,
    onExecuteCommandAction: (SearchCommandAction) -> Unit,
    onRecordQuery: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var highlightedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val results = remember(query, installedApps, hiddenComponents, searchPreferences) {
        AstraSearchIndex.query(query, installedApps, hiddenComponents, searchPreferences)
    }

    LaunchedEffect(Unit) {
        if (searchPreferences.autoFocusKeyboard) {
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Throwable) {
            }
        }
    }

    val totalNavigableApps = results.matchedApps.size

    fun launchPrimaryHighlightedResult() {
        if (query.isNotBlank()) {
            onRecordQuery(query.trim())
        }
        val appTarget = results.matchedApps.getOrNull(highlightedIndex.coerceIn(0, (totalNavigableApps - 1).coerceAtLeast(0)))
        if (appTarget != null) {
            onLaunchApp(appTarget)
            return
        }
        val firstSetting = results.matchedSettings.firstOrNull()
        if (firstSetting != null) {
            onExecuteSettingAction(firstSetting)
            return
        }
        val firstCommand = results.matchedCommands.firstOrNull()
        if (firstCommand != null) {
            onExecuteCommandAction(firstCommand)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .statusBarsPadding()
            .imePadding()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Escape -> {
                            onClose()
                            true
                        }
                        Key.DirectionDown -> {
                            if (totalNavigableApps > 0) {
                                highlightedIndex = (highlightedIndex + 1) % totalNavigableApps
                            }
                            true
                        }
                        Key.DirectionUp -> {
                            if (totalNavigableApps > 0) {
                                highlightedIndex = if (highlightedIndex - 1 < 0) totalNavigableApps - 1 else highlightedIndex - 1
                            }
                            true
                        }
                        Key.Enter, Key.NumPadEnter -> {
                            launchPrimaryHighlightedResult()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Keyboard-first Search Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = AstraShapes.SearchPill,
                color = palette.elevatedSurface,
                border = BorderStroke(1.5.dp, palette.primaryAccent.copy(alpha = 0.65f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AstraVectorIcon(
                        glyph = AstraGlyph.SEARCH,
                        tint = palette.primaryAccent,
                        size = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search installed apps, shortcuts, settings, math…",
                                style = AstraTypography.BodyL,
                                color = palette.mutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                highlightedIndex = 0
                            },
                            singleLine = true,
                            textStyle = AstraTypography.BodyL.copy(color = palette.primaryText),
                            cursorBrush = SolidColor(palette.primaryAccent),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = { launchPrimaryHighlightedResult() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                    if (query.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable {
                                    query = ""
                                    highlightedIndex = 0
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AstraVectorIcon(
                                glyph = AstraGlyph.CLOSE,
                                tint = palette.secondaryText,
                                size = 16.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "Close",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primaryAccent,
                        modifier = Modifier
                            .clickable(onClick = onClose)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Instant Calculation / Unit Conversion Result
                results.calculation?.let { calc ->
                    item(key = "calc_card") {
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = true,
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
                                        text = calc.detailLabel,
                                        style = AstraTypography.Caption,
                                        color = palette.primaryAccent
                                    )
                                    Text(
                                        text = "${calc.expression} =",
                                        style = AstraTypography.BodyM,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = calc.formattedValue,
                                    style = AstraTypography.DisplayM,
                                    color = palette.primaryText
                                )
                            }
                        }
                    }
                }

                // Real Installed Applications
                if (results.matchedApps.isNotEmpty()) {
                    item(key = "apps_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (query.isBlank()) "FREQUENT & INSTALLED APPS" else "APPLICATIONS (${results.matchedApps.size})",
                                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.secondaryText
                            )
                            Text(
                                text = "Press Enter to launch #1",
                                style = AstraTypography.Caption,
                                color = palette.primaryAccent
                            )
                        }
                    }

                    if (query.isBlank()) {
                        item(key = "apps_row_zero_query") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(results.matchedApps, key = { it.componentName }) { app ->
                                    AstraAppIcon(
                                        app = app,
                                        iconStyle = iconStyle,
                                        palette = palette,
                                        iconPipeline = iconPipeline,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onAppLongPress(app) },
                                        modifier = Modifier.width(68.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            count = results.matchedApps.size,
                            key = { idx -> results.matchedApps[idx].componentName }
                        ) { idx ->
                            val app = results.matchedApps[idx]
                            val isHighlighted = idx == highlightedIndex
                            AstraSurfaceCard(
                                palette = palette,
                                useGlass = false,
                                onClick = {
                                    onRecordQuery(query.trim())
                                    onLaunchApp(app)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { mod ->
                                        if (isHighlighted) {
                                            mod.border(1.dp, palette.primaryAccent, AstraShapes.CardMedium)
                                        } else mod
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AstraAppIcon(
                                            app = app,
                                            iconStyle = iconStyle,
                                            palette = palette,
                                            iconPipeline = iconPipeline,
                                            showLabel = false,
                                            iconScale = 0.85f,
                                            onClick = {
                                                onRecordQuery(query.trim())
                                                onLaunchApp(app)
                                            },
                                            onLongClick = { onAppLongPress(app) }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = app.label,
                                                style = AstraTypography.SectionHeader,
                                                color = palette.primaryText
                                            )
                                            Text(
                                                text = app.packageName,
                                                style = AstraTypography.Caption,
                                                color = palette.secondaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = AstraShapes.ChipPill,
                                        color = if (isHighlighted) palette.primaryAccent else palette.glassSurface
                                    ) {
                                        Text(
                                            text = if (isHighlighted) "Enter ↵" else "Launch",
                                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isHighlighted) palette.obsidian0 else palette.primaryText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Real Android App Shortcuts (LauncherApps.ShortcutQuery)
                if (results.matchedShortcuts.isNotEmpty()) {
                    item(key = "shortcuts_header") {
                        Text(
                            text = "APP SHORTCUTS",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                    }
                    items(results.matchedShortcuts, key = { "${it.first.componentName}_${it.second.id}" }) { (app, shortcut) ->
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = false,
                            onClick = { onLaunchShortcut(shortcut) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shortcut.shortLabel,
                                        style = AstraTypography.SectionHeader,
                                        color = palette.primaryText
                                    )
                                    Text(
                                        text = "${app.label} · ${shortcut.longLabel}",
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = "Shortcut",
                                    style = AstraTypography.Caption,
                                    color = palette.primaryAccent
                                )
                            }
                        }
                    }
                }

                // Launcher Actions & Commands
                if (results.matchedCommands.isNotEmpty()) {
                    item(key = "commands_header") {
                        Text(
                            text = "LAUNCHER COMMANDS",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                    }
                    items(results.matchedCommands, key = { it.id }) { cmd ->
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = false,
                            onClick = { onExecuteCommandAction(cmd) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cmd.title,
                                        style = AstraTypography.SectionHeader,
                                        color = palette.primaryText
                                    )
                                    Text(
                                        text = cmd.subtitle,
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = "Run",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryAccent
                                )
                            }
                        }
                    }
                }

                // Honest System Settings Deep-Links
                if (results.matchedSettings.isNotEmpty()) {
                    item(key = "settings_header") {
                        Text(
                            text = "ANDROID SYSTEM SETTINGS HANDOFF",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                    }
                    items(results.matchedSettings, key = { it.id }) { s ->
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = false,
                            onClick = { onExecuteSettingAction(s) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.title,
                                        style = AstraTypography.SectionHeader,
                                        color = palette.primaryText
                                    )
                                    Text(
                                        text = s.subtitle,
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = "System ↗",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryAccent
                                )
                            }
                        }
                    }
                }

                // Empty No-Match Recovery State (Section 31)
                if (results.isEmpty && query.isNotBlank()) {
                    item(key = "no_results") {
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "No installed apps or commands match \"$query\"",
                                    style = AstraTypography.SectionHeader,
                                    color = palette.primaryText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Astra only indexes real applications installed on your device. Try searching by package name or rescanning packages.",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
