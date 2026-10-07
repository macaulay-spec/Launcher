package com.astra.launcher.core.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraPlatformTest {

    @Test
    fun `package repository never fabricates fake apps when context is null`() {
        val repo = AstraPackageRepository(context = null)
        val apps = repo.refreshInstalledApps()
        assertTrue(apps.isEmpty())
        assertEquals(PackageDiscoveryState.Empty, repo.discoveryState.value)
    }

    @Test
    fun `notification stream starts empty with zero fake demo notifications`() {
        AstraNotificationStreamBus.publishNotifications(emptyList())
        assertTrue(AstraNotificationStreamBus.notifications.value.isEmpty())
        assertEquals(0, AstraNotificationStreamBus.countForPackage("com.whatsapp"))
    }
}
