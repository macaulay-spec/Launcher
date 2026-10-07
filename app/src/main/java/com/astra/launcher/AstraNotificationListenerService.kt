package com.astra.launcher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.astra.launcher.core.platform.AstraNotificationStreamBus
import com.astra.launcher.core.storage.AstraNotificationEntry

/**
 * Android NotificationListenerService (Section 22).
 * Streams real active notifications into AstraNotificationStreamBus for app icon notification badges.
 * Never fabricates demo notifications.
 */
class AstraNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        syncActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        AstraNotificationStreamBus.publishNotifications(emptyList())
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
            val mapped = activeList.mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                    ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
                val pkg = sbn.packageName
                val appLabel = try {
                    val ai = packageManager.getApplicationInfo(pkg, 0)
                    packageManager.getApplicationLabel(ai).toString()
                } catch (_: Throwable) {
                    pkg
                }
                AstraNotificationEntry(
                    id = sbn.key ?: "${pkg}_${sbn.id}",
                    packageName = pkg,
                    appName = appLabel,
                    title = title,
                    content = text,
                    postTimeMillis = sbn.postTime,
                    isClearable = sbn.isClearable
                )
            }
            AstraNotificationStreamBus.publishNotifications(mapped)
        } catch (_: Throwable) {
        }
    }
}
