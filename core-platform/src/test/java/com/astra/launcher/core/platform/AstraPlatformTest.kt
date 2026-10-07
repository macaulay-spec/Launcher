package com.astra.launcher.core.platform

import com.astra.launcher.core.storage.AppCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraPlatformTest {

    @Test
    fun `package classification assigns accurate semantic categories`() {
        assertEquals(
            AppCategory.COMMUNICATION,
            AstraPackageRepository.classifyPackage("com.android.Dialer", "Phone")
        )
        assertEquals(
            AppCategory.MEDIA,
            AstraPackageRepository.classifyPackage("com.android.camera2", "Camera")
        )
        assertEquals(
            AppCategory.PRODUCTIVITY,
            AstraPackageRepository.classifyPackage("com.android.calendar", "Calendar")
        )
    }

    @Test
    fun `restricted system toggle obeys truth principle and explains handoff`() {
        val controller = AstraSystemController(context = null)
        val feedback = controller.handleSystemTileTap("wifi")
        assertFalse(feedback.executedDirectly)
        assertTrue(feedback.userMessage.contains("Android"))
    }

    @Test
    fun `direct torch and volume toggles execute without fake error`() {
        val controller = AstraSystemController(context = null)
        val torchFeedback = controller.toggleFlashlight()
        assertTrue(torchFeedback.executedDirectly)
        assertTrue(controller.deviceStatus.value.flashlightEnabled)
    }
}
