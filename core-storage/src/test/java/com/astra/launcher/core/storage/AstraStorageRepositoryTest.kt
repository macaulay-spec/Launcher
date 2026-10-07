package com.astra.launcher.core.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraStorageRepositoryTest {

    @Test
    fun `theme preset switch updates wallpaper and mode coherently`() {
        val repo = AstraStorageRepository(context = null)
        repo.applyThemePreset(AstraThemePreset.MORNING)
        val settings = repo.themeSettings.value
        assertEquals(AstraThemePreset.MORNING, settings.themePreset)
        assertEquals(AstraWallpaperId.MORNING_MIST, settings.wallpaperId)
        assertEquals(AstraThemeMode.LIGHT, settings.themeMode)
    }

    @Test
    fun `backup export and import preserves layout and personalization`() {
        val repo = AstraStorageRepository(context = null)
        repo.applyThemePreset(AstraThemePreset.NOCTURNE)
        repo.updateHomeLayout { it.copy(gridColumns = 5, hiddenPackages = setOf("com.example.secret")) }
        val snapshot = repo.exportBackupSnapshot()

        val restoredRepo = AstraStorageRepository(context = null)
        val ok = restoredRepo.importBackupSnapshot(snapshot)
        assertTrue(ok)
        assertEquals(AstraThemePreset.NOCTURNE, restoredRepo.themeSettings.value.themePreset)
        assertEquals(5, restoredRepo.homeLayout.value.gridColumns)
        assertTrue(restoredRepo.homeLayout.value.hiddenPackages.contains("com.example.secret"))
    }

    @Test
    fun `safe defaults recovery resets corrupted state`() {
        val repo = AstraStorageRepository(context = null)
        repo.updateThemeSettings { it.copy(textScaleMultiplier = 1.4f, highContrastMode = true) }
        repo.resetToSafeDefaults()
        assertEquals(1.0f, repo.themeSettings.value.textScaleMultiplier, 0.01f)
        assertEquals(false, repo.themeSettings.value.highContrastMode)
    }
}
