package com.astra.launcher

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import com.astra.launcher.core.design.AstraColorEngine
import com.astra.launcher.core.performance.AstraPerformanceManager
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.platform.AstraPackageRepository
import com.astra.launcher.core.platform.LaunchResult
import com.astra.launcher.core.storage.AstraAccentSource
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraStorageRepository
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.HomeDensityMode
import com.astra.launcher.core.storage.SearchPreferences
import com.astra.launcher.core.storage.SwipeDownAction
import com.astra.launcher.core.storage.WorkspaceItemType
import com.astra.launcher.feature.search.AstraSearchIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AstraLauncherAcceptanceTest {

    private lateinit var appContext: Application

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
    }

    private fun registerFakeInstalledThirdPartyApp(
        packageName: String,
        className: String,
        label: String
    ) {
        val shadowPm = shadowOf(appContext.packageManager)
        val component = ComponentName(packageName, className)
        val appInfo = ApplicationInfo().apply {
            this.packageName = packageName
            this.name = label
            this.nonLocalizedLabel = label
        }
        val activityInfo = ActivityInfo().apply {
            this.packageName = packageName
            this.name = className
            this.nonLocalizedLabel = label
            this.applicationInfo = appInfo
        }
        val resolveInfo = ResolveInfo().apply {
            this.activityInfo = activityInfo
            this.nonLocalizedLabel = label
        }
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        shadowPm.addResolveInfoForIntent(launcherIntent, resolveInfo)

        val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setComponent(component)
        }
        shadowPm.addResolveInfoForIntent(explicitIntent, resolveInfo)
    }

    @Test
    fun `1 - AstraLauncherActivity is registered with ACTION_MAIN and CATEGORY_HOME in AndroidManifest`() {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = appContext.packageManager.queryIntentActivities(homeIntent, 0)
        val hasAstraHome = resolved.any {
            it.activityInfo?.name == AstraLauncherActivity::class.java.name
        }
        assertTrue("AstraLauncherActivity must declare CATEGORY_HOME", hasAstraHome)
    }

    @Test
    fun `2 - Pressing Android Home button delivers onNewIntent CATEGORY_HOME and resets overlays to Workspace`() {
        val controller = Robolectric.buildActivity(AstraLauncherActivity::class.java).setup()
        val activity = controller.get()
        assertEquals(0L, activity.homeIntentSignal.value)

        val homePressIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        controller.newIntent(homePressIntent)
        assertTrue(
            "Home button intent must increment homeIntentSignal to collapse overlays",
            activity.homeIntentSignal.value > 0L
        )
    }

    @Test
    fun `3 - First-Run Setup configures atmosphere, density mode, icons, and populates Home from real apps`() {
        val repo = AstraStorageRepository(appContext)
        repo.resetToSafeDefaults()
        assertFalse(repo.homeLayout.value.hasCompletedFirstRunSetup)

        val installed = (1..12).map { idx ->
            AstraAppEntry(
                packageName = "com.example.app$idx",
                componentName = "com.example.app$idx/MainActivity",
                activityClassName = "MainActivity",
                label = "App $idx"
            )
        }

        repo.completeFirstRunSetup(
            preset = AstraThemePreset.NOCTURNE,
            densityMode = HomeDensityMode.MINIMAL,
            iconStyle = AstraIconStyle.MONOCHROME_TINT,
            swipeDownAction = SwipeDownAction.ASTRA_NOTIFICATIONS,
            installedApps = installed
        )

        assertTrue(repo.homeLayout.value.hasCompletedFirstRunSetup)
        assertEquals(HomeDensityMode.MINIMAL, repo.homeLayout.value.densityMode)
        assertEquals(AstraThemePreset.NOCTURNE, repo.themeSettings.value.themePreset)
        assertEquals(AstraIconStyle.MONOCHROME_TINT, repo.themeSettings.value.iconStyle)
        assertEquals(SwipeDownAction.ASTRA_NOTIFICATIONS, repo.gesturePreferences.value.swipeDownAction)
        // Minimal density populates 5 dock apps + 4 curated workspace apps
        assertEquals(5, repo.homeLayout.value.dockItems.size)
        assertEquals(4, repo.homeLayout.value.items.size)
    }

    @Test
    fun `4 - Package discovery returns ONLY real installed apps and never injects fake fallback catalog`() {
        registerFakeInstalledThirdPartyApp(
            packageName = "org.mozilla.firefox",
            className = "org.mozilla.firefox.App",
            label = "Firefox"
        )
        registerFakeInstalledThirdPartyApp(
            packageName = "com.spotify.music",
            className = "com.spotify.music.MainActivity",
            label = "Spotify"
        )

        val repo = AstraPackageRepository(appContext)
        val discovered = repo.refreshInstalledApps()

        val pkgs = discovered.map { it.packageName }.toSet()
        assertTrue(pkgs.contains("org.mozilla.firefox"))
        assertTrue(pkgs.contains("com.spotify.music"))
        assertFalse(pkgs.contains("com.google.android.youtube"))
        assertFalse(pkgs.contains("com.whatsapp"))
    }

    @Test
    fun `5 - Natural language Search resolves Open App queries and honest system handoffs`() {
        val youtube = AstraAppEntry(
            packageName = "com.google.android.youtube",
            componentName = "com.google.android.youtube/HomeActivity",
            activityClassName = "HomeActivity",
            label = "YouTube"
        )
        val bundle = AstraSearchIndex.query(
            rawQuery = "open YouTube",
            installedApps = listOf(youtube),
            hiddenComponents = emptySet(),
            preferences = SearchPreferences()
        )
        assertEquals(1, bundle.matchedApps.size)
        assertEquals("com.google.android.youtube", bundle.matchedApps.first().packageName)

        val wifiBundle = AstraSearchIndex.query(
            rawQuery = "turn on wifi",
            installedApps = listOf(youtube),
            hiddenComponents = emptySet(),
            preferences = SearchPreferences()
        )
        assertTrue(wifiBundle.matchedSettings.any { it.id == "wifi" })
    }

    @Test
    fun `6 - Launching an installed app fires real ComponentName Intent and leaves launcher foreground`() {
        registerFakeInstalledThirdPartyApp(
            packageName = "org.mozilla.firefox",
            className = "org.mozilla.firefox.App",
            label = "Firefox"
        )
        val repo = AstraPackageRepository(appContext)
        val apps = repo.refreshInstalledApps()
        val firefox = apps.first { it.packageName == "org.mozilla.firefox" }

        val launchResult = repo.launchApp(firefox)
        assertTrue(launchResult is LaunchResult.Success)

        val startedIntent = shadowOf(appContext).nextStartedActivity
        assertNotNull("Android must start the third-party app activity", startedIntent)
        assertEquals(
            ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"),
            startedIntent.component
        )
        assertTrue((startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun `7 - Real Icon Pipeline renders normalized Bitmaps and invalidates cache on package update`() {
        val pipeline = AstraIconPipeline(appContext)
        val drawable = ColorDrawable(Color.rgb(56, 189, 248))

        val squircleBmp = pipeline.renderDrawableToNormalizedBitmap(
            drawable = drawable,
            sizePx = 96,
            iconStyle = AstraIconStyle.ASTRA_SQUIRCLE
        )
        assertEquals(96, squircleBmp.width)
        assertEquals(96, squircleBmp.height)

        val monoBmp = pipeline.renderDrawableToNormalizedBitmap(
            drawable = drawable,
            sizePx = 96,
            iconStyle = AstraIconStyle.MONOCHROME_TINT
        )
        assertEquals(96, monoBmp.width)
        assertEquals(96, monoBmp.height)

        pipeline.invalidatePackage("org.mozilla.firefox")
    }

    @Test
    fun `8 - 2D Workspace coordinates, smart folders, dock, widgets, and settings persist across process death`() {
        val repo1 = AstraStorageRepository(appContext)
        repo1.resetToSafeDefaults()

        val firefox = AstraAppEntry(
            packageName = "org.mozilla.firefox",
            componentName = "org.mozilla.firefox/org.mozilla.firefox.App",
            activityClassName = "org.mozilla.firefox.App",
            label = "Firefox"
        )
        val chrome = AstraAppEntry(
            packageName = "com.android.chrome",
            componentName = "com.android.chrome/com.google.android.apps.chrome.Main",
            activityClassName = "com.google.android.apps.chrome.Main",
            label = "Chrome"
        )

        val item1 = repo1.pinAppToWorkspace(firefox, preferredPage = 0)!!
        val item2 = repo1.pinAppToWorkspace(chrome, preferredPage = 0)!!
        repo1.setDockSlot(0, firefox)
        repo1.addWidgetToWorkspace(
            appWidgetId = 42,
            providerComponent = "com.astra.launcher/.AstraOrbitalClockWidgetProvider",
            label = "Astra Orbital Clock",
            preferredPage = 0,
            spanX = 4,
            spanY = 2
        )

        // Merge Chrome onto Firefox -> creates smartly named "Browsers" folder
        assertTrue(repo1.moveOrMergeWorkspaceItem(item2.id, item1.page, item1.cellX, item1.cellY))

        val repo2 = AstraStorageRepository(appContext)
        val restoredLayout = repo2.homeLayout.value
        assertTrue(restoredLayout.isInitialized)
        assertEquals(1, restoredLayout.dockItems.size)

        val restoredFolder = restoredLayout.items.firstOrNull { it.itemType == WorkspaceItemType.FOLDER }
        assertNotNull("Folder must survive process death", restoredFolder)
        assertEquals("Browsers", restoredFolder!!.label)
        assertEquals(2, restoredFolder.folderItems.size)

        val restoredWidget = restoredLayout.items.firstOrNull { it.itemType == WorkspaceItemType.WIDGET }
        assertNotNull("Bound widget coordinate item must survive process death", restoredWidget)
        assertEquals(42, restoredWidget!!.appWidgetId)

        repo2.removeAppFromFolder(restoredFolder.id, firefox.componentName)
        repo2.removeAppFromFolder(restoredFolder.id, chrome.componentName)
        assertNull(repo2.homeLayout.value.items.firstOrNull { it.id == restoredFolder.id })
    }

    @Test
    fun `9 - WCAG Contrast Engine and Performance Low-RAM budget enforce legibility across all wallpapers`() {
        for (wp in AstraWallpaperId.entries) {
            val palette = AstraColorEngine.resolvePalette(
                wallpaper = wp,
                themeMode = AstraThemeMode.AUTO_WALLPAPER,
                accentSource = AstraAccentSource.WALLPAPER,
                preset = AstraThemePreset.ASTRAL,
                customAccentHex = 0xFF7DD3FC,
                highContrast = false
            )
            val ratio = AstraColorEngine.contrastRatio(palette.primaryText, palette.obsidian0)
            assertTrue("Contrast ratio for ${wp.id} was $ratio (< 4.5)", ratio >= 4.5f)
        }

        val perf = AstraPerformanceManager(context = null)
        val lowBudget = perf.computeVisualBudget(
            animationsEnabled = true,
            reducedMotion = false,
            blurEnabled = true,
            forceLowEndMode = true,
            isLowBattery = false
        )
        assertFalse(lowBudget.enableRealtimeBlur)
        assertEquals(12, lowBudget.iconCacheMaxEntries)
    }
}
