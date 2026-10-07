package com.astra.launcher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * System Event Receiver for Boot Restoration & Charging Moments (Sections 27 & 51).
 */
class AstraSystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // AstraStorageRepository automatically restores persisted layout on cold start
            }
            Intent.ACTION_POWER_CONNECTED -> {
                ChargingEventBus.isPlugInMomentActive = true
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                ChargingEventBus.isPlugInMomentActive = false
            }
        }
    }
}

object ChargingEventBus {
    @Volatile
    var isPlugInMomentActive: Boolean = false
}

/**
 * Exported First-Party Android AppWidgetProvider (Section 20).
 */
class AstraOrbitalClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_astra_clock)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
