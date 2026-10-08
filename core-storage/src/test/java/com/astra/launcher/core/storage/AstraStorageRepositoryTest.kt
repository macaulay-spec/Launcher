package com.astra.launcher.core.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraStorageRepositoryTest {

    private fun realApp(pkg: String, cls: String, label: String) = AstraAppEntry(
        packageName = pkg,
        componentName = "$pkg/$cls",
        activityClassName = cls,
        label = label
    )

    @Test
    fun `workspace starts empty without fake apps and populates only from real installed list`() {
        val repo = AstraStorageRepository(context = null)
        assertTrue(repo.homeLayout.value.items.isEmpty())
        assertTrue(repo.homeLayout.value.dockItems.isEmpty())

        val installed = listOf(
            realApp("com.android.chrome", "com.google.android.apps.chrome.Main", "Chrome"),
            realApp("com.whatsapp", "com.whatsapp.Main", "WhatsApp"),
            realApp("com.android.settings", "com.android.settings.Settings", "Settings"),
            realApp("com.android.camera2", "com.android.camera.CameraLauncher", "Camera"),
            realApp("com.google.android.youtube", "com.google.android.youtube.HomeActivity", "YouTube"),
            realApp("org.telegram.messenger", "org.telegram.ui.LaunchActivity", "Telegram")
        )

        repo.populateInitialWorkspaceFromInstalledApps(installed)
        val layout = repo.homeLayout.value
        assertTrue(layout.isInitialized)
        assertEquals(5, layout.dockItems.size)
        assertEquals(1, layout.items.size)
        assertEquals("org.telegram.messenger/org.telegram.ui.LaunchActivity", layout.items.first().componentName)
    }

    @Test
    fun `moving app onto another app creates folder and removing all apps deletes folder`() {
        val repo = AstraStorageRepository(context = null)
        val chrome = realApp("com.android.chrome", "Main", "Chrome")
        val youtube = realApp("com.google.android.youtube", "Main", "YouTube")

        val item1 = repo.pinAppToWorkspace(chrome, preferredPage = 0)
        val item2 = repo.pinAppToWorkspace(youtube, preferredPage = 0)
        assertNotNull(item1)
        assertNotNull(item2)

        // Move item2 onto item1's exact 2D cell coordinates -> creates Folder
        val merged = repo.moveOrMergeWorkspaceItem(
            itemId = item2!!.id,
            targetPage = item1!!.page,
            targetCellX = item1.cellX,
            targetCellY = item1.cellY
        )
        assertTrue(merged)

        val folder = repo.homeLayout.value.items.firstOrNull { it.itemType == WorkspaceItemType.FOLDER }
        assertNotNull(folder)
        assertEquals(2, folder!!.folderItems.size)

        // Rename folder
        repo.renameFolder(folder.id, "Browsers & Media")
        assertEquals("Browsers & Media", repo.homeLayout.value.items.first { it.id == folder.id }.label)

        // Remove both apps from folder -> folder is automatically deleted when empty (Section 14)
        repo.removeAppFromFolder(folder.id, chrome.componentName)
        repo.removeAppFromFolder(folder.id, youtube.componentName)
        assertNull(repo.homeLayout.value.items.firstOrNull { it.id == folder.id })
    }

    @Test
    fun `uninstalling package removes its icons from workspace, folders, and dock`() {
        val repo = AstraStorageRepository(context = null)
        val chrome = realApp("com.android.chrome", "Main", "Chrome")
        val whatsapp = realApp("com.whatsapp", "Main", "WhatsApp")

        repo.pinAppToWorkspace(chrome)
        repo.setDockSlot(0, whatsapp)

        // Now WhatsApp is uninstalled -> only Chrome remains installed
        repo.reconcileWithInstalledPackages(listOf(chrome))
        assertTrue(repo.homeLayout.value.dockItems.isEmpty())
        assertEquals(1, repo.homeLayout.value.items.size)
    }

    @Test
    fun `corrupted workspace state is detected and safely recovered`() {
        val repo = AstraStorageRepository(context = null)
        repo.injectRawWorkspacePayloadForRecoveryTest("CORRUPTED_GARBAGE_DATA_WITHOUT_PIPES")
        assertTrue(repo.corruptedWorkspaceRecovered.value)
        assertTrue(repo.homeLayout.value.items.isEmpty())
    }
}
