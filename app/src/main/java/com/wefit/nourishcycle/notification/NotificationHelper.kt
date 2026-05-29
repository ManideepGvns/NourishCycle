package com.wefit.nourishcycle.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wefit.nourishcycle.MainActivity
import com.wefit.nourishcycle.R
import com.wefit.nourishcycle.data.MealCategory

object NotificationHelper {

    const val CHANNEL_ID = "meal_reminders"
    private const val CHANNEL_NAME = "Meal Reminders"
    private const val CHANNEL_DESCRIPTION =
        "Timely reminders for each meal in your fertility diet plan"

    /**
     * Creates the notification channel — safe to call multiple times (no-op if already created).
     * Must be called before posting any notification.
     */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Builds and posts a meal notification.
     *
     * @param slotIndex  0–6 meal slot index
     * @param dayIndex   0–6 day-of-cycle index (computed from current date)
     */
    fun showMealNotification(context: Context, slotIndex: Int, dayIndex: Int) {
        val content = NotificationMessages.getContent(slotIndex, dayIndex)
        val category = MealCategory.fromSlotIndex(slotIndex)

        // Tap notification → open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingOpen = PendingIntent.getActivity(
            context, slotIndex, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setContentIntent(pendingOpen)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSubText(category.displayName)
            .build()

        // Notification ID is slotIndex — only one notification per slot visible at a time
        NotificationManagerCompat.from(context).notify(slotIndex, notification)
    }

    fun areNotificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()
}
