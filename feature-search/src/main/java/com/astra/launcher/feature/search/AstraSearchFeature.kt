package com.astra.launcher.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraSearchField
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraShortcutItem

/**
 * Offline Search Index & Deterministic Command Engine (Sections 15, 16, 37, 44).
 * Prioritizes:
 * 1. Exact match
 * 2. Recently used
 * 3. Frequently used
 * 4. Contextual relevance
 * 5. Fuzzy match
 */
enum class SearchCommandActionType {
    OPEN_APP,
    SYSTEM_TILE,
    DIAL_CONTACT,
    OPEN_SETTINGS,
    START_TIMER,
    CALCULATOR
}

data class SearchCommandMatch(
    val commandText: String,
    val title: String,
    val subtitle: String,
    val actionType: SearchCommandActionType,
    val targetPayload: String
)

data class RankedAppResult(
    val app: AstraAppEntry,
    val score: Int,
    val matchReason: String
)

data class SearchQueryBundle(
    val rawQuery: String,
    val commandMatch: SearchCommandMatch?,
    val rankedApps: List<RankedAppResult>,
    val matchingShortcuts: List<AstraShortcutItem>,
    val matchingSettings: List<Pair<String, String>>
)

object AstraSearchIndex {

    private val systemSettingsIndex = listOf(
        "Wi-Fi & Connectivity" to "wifi",
        "Bluetooth Devices" to "bluetooth",
        "Battery & Power Saver" to "battery_saver",
        "Display Brightness & Adaptive Tone" to "brightness",
        "Notification Access & Privacy" to "notifications",
        "Default Home Role (Astra Launcher)" to "home_role",
        "Personalization Studio" to "personalization"
    )

    fun query(
        rawInput: String,
        apps: List<AstraAppEntry>,
        hiddenPackages: Set<String> = emptySet()
    ): SearchQueryBundle {
        val visibleApps = apps.filterNot { it.packageName in hiddenPackages }
        val q = rawInput.trim()
        if (q.isEmpty()) {
            val topSuggested = visibleApps
                .sortedWith(
                    compareByDescending<AstraAppEntry> { it.lastUsedTimestamp }
                        .thenByDescending { it.usageScore }
                )
                .take(8)
                .map { RankedAppResult(it, it.usageScore, "Frequently Used") }

            return SearchQueryBundle(
                rawQuery = "",
                commandMatch = null,
                rankedApps = topSuggested,
                matchingShortcuts = visibleApps.flatMap { it.shortcuts }.take(4),
                matchingSettings = systemSettingsIndex.take(3)
            )
        }

        val lower = q.lowercase()
        val command = parseCommand(q, lower, visibleApps)

        val rankedApps = visibleApps.mapNotNull { app ->
            val labelLower = app.label.lowercase()
            val pkgLower = app.packageName.lowercase()
            val strippedCommandTarget = lower
                .removePrefix("open ")
                .removePrefix("launch ")
                .removePrefix("find ")
                .trim()

            var score = 0
            var reason = ""

            when {
                labelLower == lower || labelLower == strippedCommandTarget -> {
                    score += 1000
                    reason = "Exact Match"
                }
                labelLower.startsWith(lower) || labelLower.startsWith(strippedCommandTarget) -> {
                    score += 650
                    reason = "Prefix Match"
                }
                app.aliases.any { it == lower || it.startsWith(lower) } -> {
                    score += 450
                    reason = "Contextual Alias"
                }
                labelLower.contains(lower) || pkgLower.contains(lower) -> {
                    score += 320
                    reason = "Name Match"
                }
                isFuzzySubsequence(lower, labelLower) -> {
                    score += 140
                    reason = "Fuzzy Match"
                }
            }

            if (score > 0) {
                if (app.lastUsedTimestamp > 0L) score += 200
                score += (app.usageScore * 2)
                RankedAppResult(app, score, reason)
            } else {
                null
            }
        }.sortedByDescending { it.score }

        val shortcuts = visibleApps
            .flatMap { it.shortcuts }
            .filter {
                it.shortLabel.lowercase().contains(lower) ||
                    it.longLabel.lowercase().contains(lower)
            }
            .take(5)

        val settings = systemSettingsIndex.filter { (title, key) ->
            title.lowercase().contains(lower) || key.lowercase().contains(lower)
        }

        return SearchQueryBundle(
            rawQuery = q,
            commandMatch = command,
            rankedApps = rankedApps,
            matchingShortcuts = shortcuts,
            matchingSettings = settings
        )
    }

    fun parseCommand(
        raw: String,
        lower: String,
        apps: List<AstraAppEntry>
    ): SearchCommandMatch? {
        // 1. Open <App>
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val targetName = lower.removePrefix("open ").removePrefix("launch ").trim()
            val matchedApp = apps.firstOrNull {
                it.label.lowercase() == targetName ||
                    it.label.lowercase().contains(targetName) ||
                    it.aliases.any { a -> a == targetName }
            }
            if (matchedApp != null) {
                return SearchCommandMatch(
                    commandText = raw,
                    title = "Launch ${matchedApp.label}",
                    subtitle = "Deterministic local app launch · ${matchedApp.packageName}",
                    actionType = SearchCommandActionType.OPEN_APP,
                    targetPayload = matchedApp.packageName
                )
            }
        }

        // 2. Turn on/off Wi-Fi, Bluetooth, Flashlight/Torch
        if (lower.contains("wi-fi") || lower.contains("wifi")) {
            return SearchCommandMatch(
                commandText = raw,
                title = "Open Wi-Fi Connectivity Panel",
                subtitle = "Direct handoff to Android Connectivity Panel",
                actionType = SearchCommandActionType.SYSTEM_TILE,
                targetPayload = "wifi"
            )
        }
        if (lower.contains("flashlight") || lower.contains("torch")) {
            return SearchCommandMatch(
                commandText = raw,
                title = "Toggle Hardware Flashlight",
                subtitle = "Immediate execution via Android CameraManager",
                actionType = SearchCommandActionType.SYSTEM_TILE,
                targetPayload = "torch"
            )
        }
        if (lower.contains("battery")) {
            return SearchCommandMatch(
                commandText = raw,
                title = "Show Battery & Power Settings",
                subtitle = "Open Android power telemetry & Battery Saver",
                actionType = SearchCommandActionType.SYSTEM_TILE,
                targetPayload = "battery_saver"
            )
        }
        if (lower.startsWith("find settings") || lower == "settings") {
            return SearchCommandMatch(
                commandText = raw,
                title = "Open Astra & System Settings",
                subtitle = "Jump directly to Settings & Personalization",
                actionType = SearchCommandActionType.OPEN_SETTINGS,
                targetPayload = "settings"
            )
        }
        if (lower.startsWith("call ")) {
            val contact = raw.substringAfter(" ").trim().ifEmpty { "Mum" }
            return SearchCommandMatch(
                commandText = raw,
                title = "Call $contact",
                subtitle = "Open Phone Dialer prepared for $contact",
                actionType = SearchCommandActionType.DIAL_CONTACT,
                targetPayload = "com.android.Dialer"
            )
        }
        if (lower.contains("timer") || lower.contains("alarm")) {
            return SearchCommandMatch(
                commandText = raw,
                title = "Start Timer / Alarm",
                subtitle = "Launch Clock & Timer utility",
                actionType = SearchCommandActionType.START_TIMER,
                targetPayload = "com.android.deskclock"
            )
        }

        return null
    }

    fun isFuzzySubsequence(needle: String, haystack: String): Boolean {
        if (needle.length < 2 || needle.length > haystack.length) return false
        var nIdx = 0
        var hIdx = 0
        while (nIdx < needle.length && hIdx < haystack.length) {
            if (needle[nIdx] == haystack[hIdx]) {
                nIdx++
            }
            hIdx++
        }
        return nIdx == needle.length
    }
}

/**
 * Global Search & Command Palette Screen (Sections 15 & 16; Frames 09, 10, 11, 12).
 * Supports immediate focus, hardware keyboard Arrow Up/Down/Enter/Esc navigation,
 * and software IME-safe layout.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AstraSearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    apps: List<AstraAppEntry>,
    hiddenPackages: Set<String>,
    recentQueries: List<String>,
    iconStyle: AstraIconStyle,
    onLaunchApp: (AstraAppEntry) -> Unit,
    onExecuteCommand: (SearchCommandMatch) -> Unit,
    onSelectSetting: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    val bundle = remember(query, apps, hiddenPackages) {
        AstraSearchIndex.query(query, apps, hiddenPackages)
    }

    var selectedIndex by remember(query) { mutableIntStateOf(0) }
    val maxIndex = (bundle.rankedApps.size - 1).coerceAtLeast(0)

    val canonicalCommands = listOf(
        "open YouTube",
        "turn on Wi-Fi",
        "find Settings",
        "call Mum",
        "show battery settings",
        "open camera",
        "start timer"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionDown -> {
                            selectedIndex = (selectedIndex + 1).coerceAtMost(maxIndex)
                            true
                        }
                        Key.DirectionUp -> {
                            selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                            true
                        }
                        Key.Enter -> {
                            val cmd = bundle.commandMatch
                            if (cmd != null) {
                                onExecuteCommand(cmd)
                            } else {
                                bundle.rankedApps.getOrNull(selectedIndex)?.app?.let(onLaunchApp)
                            }
                            true
                        }
                        Key.Escape -> {
                            onDismiss()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GLOBAL SEARCH · OFFLINE INDEX",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary
                )
                Text(
                    text = if (query.isBlank()) "Command Palette" else "Live Ranked Results",
                    style = AstraTheme.typography.titleL,
                    color = colors.textPrimary
                )
            }
            AstraIconButton(
                glyph = AstraGlyph.CLOSE,
                contentDescriptionLabel = "Close Search",
                onClick = onDismiss
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AstraSearchField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Apps, 'open camera', 'turn on Wi-Fi', 'call Mum'...",
            onClear = { onQueryChange("") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick command pills (Section 15 command examples)
        if (query.isBlank()) {
            Text(
                text = "DETERMINISTIC COMMANDS",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                canonicalCommands.forEach { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surfaceGlass)
                            .border(1.dp, colors.borderSubtle, CircleShape)
                            .clickable { onQueryChange(cmd) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cmd,
                            style = AstraTheme.typography.labelS,
                            color = colors.textSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Frame 12: Command Mode Execution Card
        bundle.commandMatch?.let { cmd ->
            Text(
                text = "INSTANT COMMAND",
                style = AstraTheme.typography.labelS,
                color = colors.accentPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                state = AstraComponentState.ACTIVE,
                onClick = { onExecuteCommand(cmd) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.accentPrimary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AstraVectorIcon(
                                glyph = AstraGlyph.SPARK,
                                size = 20.dp,
                                tint = colors.accentPrimary,
                                active = true
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cmd.title,
                                style = AstraTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = cmd.subtitle,
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accentPrimary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Run ↵",
                            style = AstraTheme.typography.labelS,
                            color = colors.surfaceBase
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Apps Section (Frame 09, 10, 11)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (query.isBlank()) "SUGGESTED & FREQUENT APPS" else "APPLICATIONS (${bundle.rankedApps.size})",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Text(
                text = "↑↓ Navigate · Enter Launch",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (query.isBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bundle.rankedApps.take(4).forEach { ranked ->
                    AstraAppIcon(
                        app = ranked.app,
                        iconStyle = iconStyle,
                        onClick = { onLaunchApp(ranked.app) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else if (bundle.rankedApps.isEmpty()) {
            // Empty Search State (Section 28)
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.SOFT_TRANSLUCENT
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No local apps match \"$query\"",
                        style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.Medium),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Search runs 100% locally for privacy. Try a broader term or reset filter.",
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            bundle.rankedApps.take(6).forEachIndexed { idx, ranked ->
                val isSelected = idx == selectedIndex
                AstraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    state = if (isSelected) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                    onClick = {
                        selectedIndex = idx
                        onLaunchApp(ranked.app)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AstraAppIcon(
                                app = ranked.app,
                                iconStyle = iconStyle,
                                showLabel = false,
                                iconScale = 0.78f,
                                onClick = { onLaunchApp(ranked.app) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ranked.app.label,
                                    style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${ranked.app.category.label} · ${ranked.matchReason}",
                                    style = AstraTheme.typography.labelS,
                                    color = if (isSelected) colors.accentPrimary else colors.textTertiary
                                )
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.accentPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Selected ↵",
                                    style = AstraTheme.typography.labelS,
                                    color = colors.accentPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Shortcuts & System Settings domains
        if (bundle.matchingShortcuts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "DEEP SHORTCUTS",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))
            bundle.matchingShortcuts.forEach { shortcut ->
                AstraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = {
                        apps.firstOrNull { it.packageName == shortcut.packageName }?.let(onLaunchApp)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(shortcut.shortLabel, style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                            Text(shortcut.longLabel, style = AstraTheme.typography.labelS, color = colors.textSecondary)
                        }
                        AstraVectorIcon(glyph = AstraGlyph.CHEVRON_RIGHT, size = 14.dp, tint = colors.accentPrimary)
                    }
                }
            }
        }

        if (bundle.matchingSettings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "SYSTEM & ASTRA CONTROLS",
                style = AstraTheme.typography.labelS,
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))
            bundle.matchingSettings.forEach { (title, key) ->
                AstraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onSelectSetting(key) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AstraVectorIcon(glyph = AstraGlyph.SETTINGS, size = 16.dp, tint = colors.accentPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(title, style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                        }
                        AstraVectorIcon(glyph = AstraGlyph.CHEVRON_RIGHT, size = 14.dp, tint = colors.textTertiary)
                    }
                }
            }
        }
    }
}
