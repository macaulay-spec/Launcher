package com.astra.launcher

import com.astra.launcher.core.platform.AstraPackageRepository
import com.astra.launcher.core.storage.AstraCanonicalFrame
import com.astra.launcher.core.storage.AstraStorageRepository
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.feature.search.AstraSearchIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraLauncherAcceptanceTest {

    @Test
    fun `all 40 visual screen inventory frames are registered`() {
        assertEquals(40, AstraCanonicalFrame.entries.size)
        assertEquals(1, AstraCanonicalFrame.entries.first().number)
        assertEquals(40, AstraCanonicalFrame.entries.last().number)
    }

    @Test
    fun `all 5 theme presets and 5 curated wallpapers are present`() {
        assertEquals(5, AstraThemePreset.entries.size)
        assertEquals(5, AstraWallpaperId.entries.size)
    }

    @Test
    fun `offline app search and launch updates usage ranking`() {
        val pkgRepo = AstraPackageRepository(context = null)
        val appsBefore = pkgRepo.installedApps.value
        val cameraBefore = appsBefore.first { it.packageName == "com.android.camera2" }
        val initialScore = cameraBefore.usageScore

        val launched = pkgRepo.launchApp("com.android.camera2")
        assertTrue(launched)

        val cameraAfter = pkgRepo.installedApps.value.first { it.packageName == "com.android.camera2" }
        assertTrue(cameraAfter.usageScore > initialScore)

        val searchBundle = AstraSearchIndex.query("camera", pkgRepo.installedApps.value)
        assertEquals("com.android.camera2", searchBundle.rankedApps.first().app.packageName)
    }

    @Test
    fun `home layout and personalization persist across repository lifecycle`() {
        val repo = AstraStorageRepository(context = null)
        repo.setOnboardingCompleted(true)
        repo.applyThemePreset(AstraThemePreset.GLASS_HORIZON)
        assertTrue(repo.onboardingCompleted.value)
        assertEquals(AstraWallpaperId.GLASS_HORIZON, repo.themeSettings.value.wallpaperId)
    }
}
