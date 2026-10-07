package com.astra.launcher.feature.search

import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.SearchPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraSearchIndexTest {

    private val sampleApps = listOf(
        AstraAppEntry(
            packageName = "com.android.chrome",
            componentName = "com.android.chrome/Main",
            activityClassName = "Main",
            label = "Chrome",
            usageScore = 10
        ),
        AstraAppEntry(
            packageName = "com.whatsapp",
            componentName = "com.whatsapp/Main",
            activityClassName = "Main",
            label = "WhatsApp",
            usageScore = 25
        )
    )

    @Test
    fun `search matches real installed apps by label and package name`() {
        val prefs = SearchPreferences()
        val byLabel = AstraSearchIndex.query("wh", sampleApps, emptySet(), prefs)
        assertEquals(1, byLabel.matchedApps.size)
        assertEquals("com.whatsapp", byLabel.matchedApps.first().packageName)

        val byPkg = AstraSearchIndex.query("chrome", sampleApps, emptySet(), prefs)
        assertEquals(1, byPkg.matchedApps.size)
        assertEquals("Chrome", byPkg.matchedApps.first().label)
    }

    @Test
    fun `search excludes hidden components`() {
        val prefs = SearchPreferences()
        val result = AstraSearchIndex.query(
            "WhatsApp",
            sampleApps,
            setOf("com.whatsapp/Main"),
            prefs
        )
        assertTrue(result.matchedApps.isEmpty())
    }

    @Test
    fun `deterministic math and unit conversion evaluate accurately`() {
        val math = AstraSearchIndex.evaluateExpressionOrConversion("24 * 7")
        assertNotNull(math)
        assertEquals("168", math!!.formattedValue)

        val unit = AstraSearchIndex.evaluateExpressionOrConversion("100 c to f")
        assertNotNull(unit)
        assertEquals("212.00 °F", unit!!.formattedValue)
    }
}
