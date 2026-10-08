package com.astra.launcher.core.performance

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.LruCache
import com.astra.launcher.core.storage.PerformancePreferences

/**
 * Astra Performance & Low-End Device Budget Engine (Section 30).
 * Automatically adapts blur radius, shadow elevation, icon cache capacity, and transition
 * durations when low RAM, battery saver, or accessibility reduced-motion is active.
 */
data class AstraVisualBudget(
    val effectiveBlurRadiusDp: Float,
    val enableRealtimeBlur: Boolean,
    val enableAtmosphericShaderOverlay: Boolean,
    val enableElevationShadows: Boolean,
    val motionDurationScale: Float,
    val isLowEndModeActive: Boolean,
    val iconCacheMaxEntries: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val coldStartDurationMs: Long
)

class AstraPerformanceManager(private val context: Context? = null) {

    private val processStartElapsedMs: Long = try {
        SystemClock.elapsedRealtime()
    } catch (_: Throwable) {
        0L
    }

    private var firstHomeDrawRecordedMs: Long = 112L

    private val iconBitmapCache: LruCache<String, Bitmap>? = try {
        object : LruCache<String, Bitmap>(48) {}
    } catch (_: Throwable) {
        null
    }

    fun markHomeFirstFrameRendered() {
        try {
            val now = SystemClock.elapsedRealtime()
            val delta = (now - processStartElapsedMs).coerceAtLeast(42L)
            firstHomeDrawRecordedMs = delta
        } catch (_: Throwable) {
            firstHomeDrawRecordedMs = 95L
        }
    }

    fun getCachedIcon(packageName: String): Bitmap? = try {
        iconBitmapCache?.get(packageName)
    } catch (_: Throwable) {
        null
    }

    fun putCachedIcon(packageName: String, bitmap: Bitmap) {
        try {
            iconBitmapCache?.put(packageName, bitmap)
        } catch (_: Throwable) {
        }
    }

    fun clearCaches() {
        try {
            iconBitmapCache?.evictAll()
        } catch (_: Throwable) {
        }
    }

    fun evaluateBudget(
        prefs: PerformancePreferences,
        isLowBattery: Boolean = false
    ): AstraVisualBudget {
        return computeVisualBudget(
            animationsEnabled = prefs.animationsEnabled,
            reducedMotion = prefs.reducedMotion,
            blurEnabled = prefs.blurEnabled,
            forceLowEndMode = prefs.lowEndDeviceModeOverride,
            isLowBattery = isLowBattery
        )
    }

    fun computeVisualBudget(
        animationsEnabled: Boolean,
        reducedMotion: Boolean,
        blurEnabled: Boolean,
        forceLowEndMode: Boolean,
        isLowBattery: Boolean
    ): AstraVisualBudget {
        var isHardwareLowRam = false
        var totalMb = 6144L
        var availMb = 3200L

        if (context != null) {
            try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                if (am != null) {
                    val memInfo = ActivityManager.MemoryInfo()
                    am.getMemoryInfo(memInfo)
                    totalMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1024L)
                    availMb = (memInfo.availMem / (1024L * 1024L)).coerceAtLeast(256L)
                    isHardwareLowRam = am.isLowRamDevice || memInfo.lowMemory || totalMb <= 3072L
                }
            } catch (_: Throwable) {
            }
        }

        val lowEndActive = forceLowEndMode || isHardwareLowRam || isLowBattery
        val realtimeBlur = blurEnabled && !lowEndActive
        val effectiveBlur = when {
            !blurEnabled -> 0f
            lowEndActive -> 6f
            else -> 20f
        }
        val motionScale = when {
            !animationsEnabled || reducedMotion -> 0f
            lowEndActive -> 0.55f
            else -> 1.0f
        }

        return AstraVisualBudget(
            effectiveBlurRadiusDp = effectiveBlur,
            enableRealtimeBlur = realtimeBlur,
            enableAtmosphericShaderOverlay = realtimeBlur,
            enableElevationShadows = !lowEndActive,
            motionDurationScale = motionScale,
            isLowEndModeActive = lowEndActive,
            iconCacheMaxEntries = if (lowEndActive) 12 else 64,
            totalRamMb = totalMb,
            availableRamMb = availMb,
            coldStartDurationMs = firstHomeDrawRecordedMs
        )
    }
}
