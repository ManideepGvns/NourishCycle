package com.wefit.nourishcycle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.wefit.nourishcycle.navigation.AppNavigation
import com.wefit.nourishcycle.notification.MealNotificationScheduler
import com.wefit.nourishcycle.notification.NotificationHelper
import com.wefit.nourishcycle.ui.theme.NourishCycleTheme

class MainActivity : ComponentActivity() {

    // Android 13+ requires explicit POST_NOTIFICATIONS permission at runtime.
    // Alarms are scheduled regardless — only the displayed notification requires the permission.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // No-op: alarms are already scheduled in onCreate regardless of outcome (L7 fix).
        // If the user denies, MealNotificationReceiver will silently fail to show the
        // notification toast — the alarm still fires and reschedules itself.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel (safe to call on every launch)
        NotificationHelper.createChannel(this)

        // Schedule alarms first — alarms ≠ notifications, no permission required (L7 fix)
        MealNotificationScheduler.scheduleAll(this)

        // Then ask for POST_NOTIFICATIONS permission on Android 13+ so the receiver can
        // actually show the heads-up notification when an alarm fires
        requestNotificationPermission()

        setContent {
            NourishCycleTheme {
                AppNavigation()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

