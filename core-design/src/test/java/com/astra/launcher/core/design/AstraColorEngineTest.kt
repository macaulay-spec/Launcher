package com.astra.launcher.core.design

import androidx.compose.ui.graphics.Color
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.ThemeSettings
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraColorEngineTest {

    @Test
    fun `derived accent passes contrast check on both dark and light surfaces`() {
        for (wallpaper in AstraWallpaperId.entries) {
            val darkColors = AstraColorEngine.buildSemanticColors(
                ThemeSettings(wallpaperId = wallpaper, themeMode = AstraThemeMode.DARK)
            )
            val lightColors = AstraColorEngine.buildSemanticColors(
                ThemeSettings(wallpaperId = wallpaper, themeMode = AstraThemeMode.LIGHT)
            )
            val darkRatio = AstraColorEngine.contrastRatio(darkColors.accentPrimary, darkColors.surfaceRaised)
            val lightRatio = AstraColorEngine.contrastRatio(lightColors.accentPrimary, lightColors.surfaceBase)
            assertTrue("Dark accent contrast ($darkRatio) must be >= 3.0", darkRatio >= 3.0f)
            assertTrue("Light accent contrast ($lightRatio) must be >= 3.0", lightRatio >= 3.0f)
        }
    }

    @Test
    fun `primary text contrast meets WCAG AA against base surface`() {
        val darkColors = AstraColorEngine.buildSemanticColors(ThemeSettings(themeMode = AstraThemeMode.DARK))
        val ratio = AstraColorEngine.contrastRatio(darkColors.textPrimary, darkColors.surfaceBase)
        assertTrue("Primary text contrast ($ratio) must exceed 10:1", ratio >= 10.0f)
    }
}
