package com.wefit.nourishcycle.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wefit.nourishcycle.data.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * BroadcastReceiver that fires when a meal alarm goes off.
 *
 * Responsibilities:
 *  1. Determine the current day-of-cycle index from DataStore cycle start date
 *  2. Post the creative meal notification for that slot + day
 *  3. Reschedule the alarm for tomorrow (perpetual daily recurrence)
 */
class MealNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MEAL_REMINDER = "com.wefit.nourishcycle.MEAL_REMINDER"
        const val EXTRA_SLOT_INDEX = "slot_index"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MEAL_REMINDER) return

        val slotIndex = intent.getIntExtra(EXTRA_SLOT_INDEX, -1)
        if (slotIndex !in 0..6) return

        // Ensure the notification channel exists
        NotificationHelper.createChannel(context)

        // Compute day index from cycle start date asynchronously
        // goAsync() gives us a grace period to complete async work in a BroadcastReceiver
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = PreferencesRepository(context)
                val cycleStart = prefs.cycleStartDate.first()
                val today = LocalDate.now()
                val dayIndex = prefs.dayIndex(today, cycleStart)

                // Show the notification only if notifications are enabled
                if (NotificationHelper.areNotificationsEnabled(context)) {
                    NotificationHelper.showMealNotification(context, slotIndex, dayIndex)
                }
            } finally {
                // Reschedule for the same slot tomorrow — maintains perpetual daily schedule
                MealNotificationScheduler.scheduleSlot(context, slotIndex)
                pendingResult.finish()
            }
        }
    }
}
