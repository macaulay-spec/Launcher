package com.astra.launcher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/**
 * Exported First-Party Android AppWidgetProvider (Section 13).
 * Uses real Android `<TextClock>` views in `R.layout.widget_astra_clock`.
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
