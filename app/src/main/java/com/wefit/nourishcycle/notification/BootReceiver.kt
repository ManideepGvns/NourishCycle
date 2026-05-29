package com.wefit.nourishcycle.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reschedules all 7 meal alarms after the device reboots.
 *
 * AlarmManager alarms are cleared on reboot; this receiver restores them.
 * Requires RECEIVE_BOOT_COMPLETED permission in AndroidManifest.xml.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            NotificationHelper.createChannel(context)
            MealNotificationScheduler.scheduleAll(context)
        }
    }
}
