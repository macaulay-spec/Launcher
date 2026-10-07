package com.astra.launcher.core.performance

import com.astra.launcher.core.storage.PerformancePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraPerformanceManagerTest {

    @Test
    fun `low battery or low-end override gracefully degrades blur and live effects`() {
        val manager = AstraPerformanceManager(context = null)
        val budget = manager.evaluateBudget(
            prefs = PerformancePreferences(lowEndDeviceModeOverride = true),
            isLowBattery = false
        )
        assertTrue(budget.isLowEndModeActive)
        assertFalse(budget.enableAtmosphericShaderOverlay)
        assertEquals(6f, budget.effectiveBlurRadiusDp, 0.01f)
    }

    @Test
    fun `reduced motion zeroes animation duration scale`() {
        val manager = AstraPerformanceManager(context = null)
        val budget = manager.evaluateBudget(
            prefs = PerformancePreferences(reducedMotion = true),
            isLowBattery = false
        )
        assertEquals(0f, budget.motionDurationScale, 0.01f)
    }
}
