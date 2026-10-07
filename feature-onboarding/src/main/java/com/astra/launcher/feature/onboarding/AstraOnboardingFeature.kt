package com.astra.launcher.feature.onboarding

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraBrandMark
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraSegmentedControl
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.ThemeSettings

/**
 * 6-Step Cinematic Onboarding Sequence (Section 25; Frames 01, 02, 03).
 */
@Composable
fun AstraOnboardingScreen(
    themeSettings: ThemeSettings,
    isDefaultHome: Boolean,
    initialStep: Int = 1,
    onApplyPreset: (AstraThemePreset) -> Unit,
    onSelectClock: (AstraClockStyle) -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var step by remember(initialStep) { mutableIntStateOf(initialStep.coerceIn(1, 6)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 22.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Indicator Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ASTRA SETUP · STEP $step OF 6",
                style = AstraTheme.typography.labelS,
                color = colors.accentPrimary
            )
            Text(
                text = "Skip to Home →",
                style = AstraTheme.typography.labelS,
                color = colors.textSecondary,
                modifier = Modifier.clickable { onCompleteOnboarding() }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        when (step) {
            1 -> {
                // Step 1: Welcome / Brand Moment (Frame 01 & 02)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AstraBrandMark(size = 96.dp, luminousMoment = true)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Astra Launcher",
                        style = AstraTheme.typography.headlineXl,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cinematic calm + intelligent utility + spatial depth + restrained personalization.",
                        style = AstraTheme.typography.bodyL,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "One Coherent Visual World",
                                style = AstraTheme.typography.titleM,
                                color = colors.accentPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Connects wallpaper atmosphere, lock surface, home, offline command search, notifications, spatial controls, widgets, and system telemetry.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            2 -> {
                // Step 2: Make Astra Home via Android RoleManager.ROLE_HOME (Section 25)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AstraBrandMark(size = 64.dp, luminousMoment = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Make Astra Your Home",
                        style = AstraTheme.typography.headlineL,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Astra uses Android's official RoleManager.ROLE_HOME contract (API 29+) so pressing the Home gesture returns to Astra reliably without dark patterns.",
                        style = AstraTheme.typography.bodyM,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        state = AstraComponentState.ACTIVE,
                        onClick = onRequestDefaultHomeRole
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isDefaultHome) "✓ Astra is Currently Your Default Home" else "Request Android Home Role",
                                style = AstraTheme.typography.titleM,
                                color = colors.accentPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to open Android's system default Home selector. You can change this anytime in Android Settings.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            3 -> {
                // Step 3: Choose your atmosphere (Frame 03)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose Your Atmosphere",
                        style = AstraTheme.typography.headlineL,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select one of Astra's five signature atmospheres. The wallpaper derives surface temperature and contrast-checked accents.",
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AstraThemePreset.entries.forEach { preset ->
                        val selected = themeSettings.themePreset == preset
                        AstraCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                            state = if (selected) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                            onClick = { onApplyPreset(preset) }
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
                                        text = "${preset.displayName} · ${preset.defaultWallpaper.title}",
                                        style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = preset.subtitle,
                                        style = AstraTheme.typography.bodyS,
                                        color = colors.textSecondary
                                    )
                                }
                                if (selected) {
                                    Text("Selected", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Step 4: Arrange your essentials
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Arrange Your Essentials",
                        style = AstraTheme.typography.headlineL,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Default Home remains calm and uncrowded: adaptive clock, 6 priority shortcuts, 2 intelligent widgets, and a 5-icon spatial dock.",
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SELECT CLOCK TYPOGRAPHY", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    AstraSegmentedControl(
                        items = AstraClockStyle.entries,
                        selectedItem = themeSettings.clockStyle,
                        labelProvider = { it.label },
                        onSelect = onSelectClock
                    )
                }
            }

            5 -> {
                // Step 5: Search + Controls + Optional Notification Consent
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fast Paths & Honest Controls",
                        style = AstraTheme.typography.headlineL,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("• Swipe Up → App Library with fast alphabet rail", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• Tap Search Pill → Offline Command Palette ('open camera', 'call Mum')", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• Swipe Down / Top Bar → Notification Stream & Spatial Control Plane", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• Long-Press Home → Live Home Editor & Personalization Studio", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        onClick = onRequestNotificationAccess
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Optional: Grant Notification Access", style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold), color = colors.accentPrimary)
                            Text(
                                "Allows Astra to display grouped notifications. If skipped, Astra works normally with zero degradation.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            6 -> {
                // Step 6: Finish
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AstraBrandMark(size = 84.dp, luminousMoment = true)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Your Atmosphere is Ready",
                        style = AstraTheme.typography.headlineL,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wake → see atmosphere → understand time → reach what matters → search anything → launch instantly.",
                        style = AstraTheme.typography.bodyM,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surfaceGlass)
                        .border(1.dp, colors.borderSubtle, CircleShape)
                        .clickable { step-- }
                        .padding(horizontal = 20.dp, vertical = 11.dp)
                ) {
                    Text("Back", style = AstraTheme.typography.labelL, color = colors.textPrimary)
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..6) {
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (i == step) colors.accentPrimary else colors.borderSubtle)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.accentPrimary)
                    .clickable {
                        if (step < 6) step++ else onCompleteOnboarding()
                    }
                    .padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Text(
                    text = if (step < 6) "Continue" else "Enter Astra Home",
                    style = AstraTheme.typography.labelL,
                    color = colors.surfaceBase
                )
            }
        }
    }
}
