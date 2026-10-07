package com.astra.launcher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.astra.launcher.core.platform.AstraNotificationBridge
import com.astra.launcher.core.storage.AstraNotificationEntry
import com.astra.launcher.core.storage.NotificationPriorityBucket

/**
 * Android NotificationListenerService (Section 17 & 34.3).
 * Streams real device notifications into AstraNotificationBridge when user grants Notification Access.
 */
class AstraNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        AstraNotificationBridge.onListenerConnected(true)
        syncActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        AstraNotificationBridge.onListenerConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        syncActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        syncActiveNotifications()
    }

    private fun syncActiveNotifications() {
        try {
            val activeList = activeNotifications ?: return
            val mapped = activeList.take(25).mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                    ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
                val pkg = sbn.packageName
                val appLabel = try {
                    val ai = packageManager.getApplicationInfo(pkg, 0)
                    packageManager.getApplicationLabel(ai).toString()
                } catch (_: Throwable) {
                    pkg.substringAfterLast('.')
                }
                val isSensitive = sbn.notification.visibility == Notification.VISIBILITY_PRIVATE ||
                    sbn.notification.visibility == Notification.VISIBILITY_SECRET
                val bucket = when {
                    sbn.isOngoing || sbn.notification.priority >= Notification.PRIORITY_HIGH ->
                        NotificationPriorityBucket.URGENT
                    sbn.notification.priority <= Notification.PRIORITY_LOW ->
                        NotificationPriorityBucket.SILENT
                    else -> NotificationPriorityBucket.REGULAR
                }
                AstraNotificationEntry(
                    id = sbn.key ?: "${pkg}_${sbn.id}",
                    packageName = pkg,
                    appName = appLabel,
                    title = title,
                    content = text.ifBlank { "Tap to open $appLabel" },
                    timestampLabel = "Now",
                    bucket = bucket,
                    isSensitive = isSensitive,
                    isClearable = sbn.isClearable
                )
            }
            AstraNotificationBridge.updateFromSystemNotifications(mapped)
        } catch (_: Throwable) {
            // Safe fallback if notification ranking fails
        }
    }
}
