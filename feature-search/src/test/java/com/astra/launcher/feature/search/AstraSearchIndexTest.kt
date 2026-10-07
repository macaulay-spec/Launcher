package com.astra.launcher.feature.search

import com.astra.launcher.core.platform.AstraPackageRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AstraSearchIndexTest {

    private val sampleApps = AstraPackageRepository.defaultCatalog()

    @Test
    fun `exact match ranks highest in local search index`() {
        val result = AstraSearchIndex.query("Camera", sampleApps)
        assertTrue(result.rankedApps.isNotEmpty())
        assertEquals("Camera", result.rankedApps.first().app.label)
        assertEquals("Exact Match", result.rankedApps.first().matchReason)
    }

    @Test
    fun `deterministic natural language commands resolve without cloud dependency`() {
        val yt = AstraSearchIndex.query("open YouTube", sampleApps)
        assertNotNull(yt.commandMatch)
        assertEquals(SearchCommandActionType.OPEN_APP, yt.commandMatch?.actionType)
        assertEquals("com.google.android.youtube", yt.commandMatch?.targetPayload)

        val wifi = AstraSearchIndex.query("turn on Wi-Fi", sampleApps)
        assertNotNull(wifi.commandMatch)
        assertEquals("wifi", wifi.commandMatch?.targetPayload)

        val mum = AstraSearchIndex.query("call Mum", sampleApps)
        assertNotNull(mum.commandMatch)
        assertEquals(SearchCommandActionType.DIAL_CONTACT, mum.commandMatch?.actionType)
    }

    @Test
    fun `fuzzy subsequence matching finds apps on fast typing`() {
        val result = AstraSearchIndex.query("clndr", sampleApps)
        assertTrue(result.rankedApps.any { it.app.label == "Calendar" })
    }
}
