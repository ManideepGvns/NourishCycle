package com.wefit.nourishcycle.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Schedules exact meal-time alarms using AlarmManager.setExactAndAllowWhileIdle().
 *
 * Design:
 *  - One alarm per meal slot (requestCode = slotIndex, 0–6)
 *  - Each alarm fires once; MealNotificationReceiver reschedules it for the next day
 *  - On first call (app open or reboot), schedules all 7 slots for their next occurrence
 *  - On API 31+, checks canScheduleExactAlarms() and falls back to setAndAllowWhileIdle
 *    if the user has not granted the exact-alarm capability (R1 fix).
 */
object MealNotificationScheduler {

    /**
     * The 7 meal times matching the DietPlanData slot order.
     * Format: slotIndex to (hour24, minute)
     */
    val MEAL_TIMES = listOf(
        0 to Pair(7, 0),    // 7:00 AM  — Morning Drink
        1 to Pair(8, 30),   // 8:30 AM  — Breakfast
        2 to Pair(11, 0),   // 11:00 AM — Mid-Morning Snack
        3 to Pair(13, 0),   // 1:00 PM  — Lunch
        4 to Pair(16, 30),  // 4:30 PM  — Evening Snack
        5 to Pair(19, 30),  // 7:30 PM  — Dinner
        6 to Pair(21, 30)   // 9:30 PM  — Night Drink
    )

    /** Schedule all 7 meal alarms for their next upcoming time. */
    fun scheduleAll(context: Context) {
        MEAL_TIMES.forEach { (slotIndex, _) ->
            scheduleSlot(context, slotIndex)
        }
    }

    /**
     * Schedule the alarm for a single slot at its next upcoming occurrence.
     * If the meal time has already passed today, schedules for tomorrow.
     */
    fun scheduleSlot(context: Context, slotIndex: Int) {
        val (hour, minute) = MEAL_TIMES.firstOrNull { it.first == slotIndex }?.second
            ?: return

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If this time has already passed today, push to tomorrow
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, slotIndex)

        // On API 31+ the user must grant exact-alarm capability. If not granted yet,
        // fall back to an inexact alarm so the notification still fires (within ~5 min) (R1 fix).
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                pendingIntent
            )
        }
    }

    /** Cancel all scheduled meal alarms (e.g. user turns off notifications). */
    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        MEAL_TIMES.forEach { (slotIndex, _) ->
            alarmManager.cancel(buildPendingIntent(context, slotIndex))
        }
    }

    private fun buildPendingIntent(context: Context, slotIndex: Int): PendingIntent {
        val intent = Intent(context, MealNotificationReceiver::class.java).apply {
            action = MealNotificationReceiver.ACTION_MEAL_REMINDER
            putExtra(MealNotificationReceiver.EXTRA_SLOT_INDEX, slotIndex)
        }
        return PendingIntent.getBroadcast(
            context,
            slotIndex,          // unique requestCode per slot — avoids collision
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
