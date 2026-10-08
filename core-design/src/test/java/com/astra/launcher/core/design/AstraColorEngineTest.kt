package com.astra.launcher.core.design

import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.ThemeSettings
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraColorEngineTest {

    @Test
    fun `derived accent passes contrast check across all bundled wallpapers`() {
        for (wallpaper in AstraWallpaperId.entries) {
            val palette = AstraColorEngine.buildSemanticColors(
                ThemeSettings(wallpaperId = wallpaper, themeMode = AstraThemeMode.DARK)
            )
            val ratio = AstraColorEngine.contrastRatio(palette.primaryAccent, palette.obsidian0)
            assertTrue("Accent contrast ($ratio) must be >= 3.0", ratio >= 3.0f)
        }
    }

    @Test
    fun `primary text contrast meets WCAG AA against base surface`() {
        val palette = AstraColorEngine.buildSemanticColors(ThemeSettings(themeMode = AstraThemeMode.DARK))
        val ratio = AstraColorEngine.contrastRatio(palette.primaryText, palette.obsidian0)
        assertTrue("Primary text contrast ($ratio) must exceed 10:1", ratio >= 10.0f)
    }
}
