package com.astra.launcher.core.platform

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.UserManager
import android.util.LruCache
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraIconStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real Application Icon Pipeline (Section 6 — REAL APPLICATION ICONS MANDATORY):
 * LauncherActivityInfo / PackageManager Drawable
 *   -> Adaptive / legacy / monochrome Drawable extraction
 *   -> Optical size normalization (bounded Bitmap size to protect RAM)
 *   -> Optional Astra icon treatment (preserving the real app icon)
 *   -> LruCache memory cache with live package invalidation
 */
class AstraIconPipeline(private val context: Context?) {

    private val cacheSizeKb: Int = run {
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        (maxMemoryKb / 16).coerceIn(2048, 16384)
    }

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return (value.allocationByteCount / 1024).coerceAtLeast(1)
        }
    }

    fun getCachedIcon(app: AstraAppEntry, iconStyle: AstraIconStyle): Bitmap? {
        val key = buildCacheKey(app, iconStyle)
        synchronized(memoryCache) {
            return memoryCache.get(key)
        }
    }

    suspend fun loadAppIconBitmap(
        app: AstraAppEntry,
        iconStyle: AstraIconStyle,
        accentColorArgb: Int = 0xFF7DD3FC.toInt()
    ): Bitmap? = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext null
        val key = buildCacheKey(app, iconStyle)
        synchronized(memoryCache) {
            memoryCache.get(key)?.let { return@withContext it }
        }

        val rawDrawable = resolveRealApplicationDrawable(ctx, app) ?: return@withContext null
        val density = ctx.resources.displayMetrics.density
        val targetSizePx = (56f * density).toInt().coerceIn(72, 168)

        val rendered = renderDrawableToNormalizedBitmap(
            drawable = rawDrawable,
            sizePx = targetSizePx,
            iconStyle = iconStyle,
            accentColorArgb = accentColorArgb
        )

        synchronized(memoryCache) {
            memoryCache.put(key, rendered)
        }
        rendered
    }

    /**
     * Resolves the REAL application icon Drawable from Android's LauncherApps or PackageManager.
     * Never substitutes generic vector glyphs for installed apps.
     */
    fun resolveRealApplicationDrawable(ctx: Context, app: AstraAppEntry): Drawable? {
        val densityDpi = ctx.resources.displayMetrics.densityDpi
        return try {
            val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            val userManager = ctx.getSystemService(Context.USER_SERVICE) as? UserManager
            if (launcherApps != null && userManager != null) {
                val userHandle = userManager.getUserForSerialNumber(app.userSerial)
                    ?: android.os.Process.myUserHandle()
                val activities = launcherApps.getActivityList(app.packageName, userHandle)
                val matched = activities.firstOrNull {
                    it.componentName.flattenToString() == app.componentName ||
                        it.componentName.className == app.activityClassName
                } ?: activities.firstOrNull()

                if (matched != null) {
                    return matched.getBadgedIcon(densityDpi)
                }
            }

            val pm = ctx.packageManager
            val comp = ComponentName.unflattenFromString(app.componentName)
                ?: ComponentName(app.packageName, app.activityClassName)
            try {
                pm.getActivityIcon(comp)
            } catch (_: Throwable) {
                pm.getApplicationIcon(app.packageName)
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun renderDrawableToNormalizedBitmap(
        drawable: Drawable,
        sizePx: Int,
        iconStyle: AstraIconStyle,
        accentColorArgb: Int = 0xFF7DD3FC.toInt()
    ): Bitmap {
        val safeSize = sizePx.coerceIn(48, 192)
        val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (iconStyle == AstraIconStyle.MONOCHROME_TINT) {
            // On Android 13+ (API 33+), use the app's official monochrome AdaptiveIconDrawable layer if provided
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && drawable is AdaptiveIconDrawable) {
                val mono = drawable.monochrome
                if (mono != null) {
                    val mutated = mono.mutate()
                    mutated.setBounds(0, 0, safeSize, safeSize)
                    mutated.colorFilter = PorterDuffColorFilter(accentColorArgb, PorterDuff.Mode.SRC_IN)
                    mutated.draw(canvas)
                    return bitmap
                }
            }
            // Otherwise render the real icon with a desaturating + subtle atmospheric tint matrix
            val rawBitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
            val rawCanvas = Canvas(rawBitmap)
            drawable.setBounds(0, 0, safeSize, safeSize)
            drawable.draw(rawCanvas)

            val saturationMatrix = ColorMatrix().apply { setSaturation(0.15f) }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(saturationMatrix)
            }
            canvas.drawBitmap(rawBitmap, 0f, 0f, paint)
            return bitmap
        }

        // Standard fidelity rendering of the real app icon
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val src = drawable.bitmap
            val scaled = Bitmap.createScaledBitmap(src, safeSize, safeSize, true)
            canvas.drawBitmap(scaled, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
        } else {
            drawable.setBounds(0, 0, safeSize, safeSize)
            drawable.draw(canvas)
        }
        return bitmap
    }

    /**
     * Invalidates cached icons for a specific package on install/update/uninstall (Section 6 & 7).
     */
    fun invalidatePackage(packageName: String) {
        synchronized(memoryCache) {
            val snapshot = memoryCache.snapshot().keys.toList()
            snapshot.forEach { key ->
                if (key.startsWith("$packageName/") || key.startsWith("${packageName}_")) {
                    memoryCache.remove(key)
                }
            }
        }
    }

    fun clearAll() {
        synchronized(memoryCache) {
            memoryCache.evictAll()
        }
    }

    private fun buildCacheKey(app: AstraAppEntry, iconStyle: AstraIconStyle): String {
        return "${app.componentName}_${app.userSerial}_${app.versionCode}_${iconStyle.id}"
    }
}
