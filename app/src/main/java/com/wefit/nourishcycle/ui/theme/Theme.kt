package com.wefit.nourishcycle.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NourishColorScheme = darkColorScheme(
    primary = LimeGreen,
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1A4A00),
    onPrimaryContainer = LimeGreenLight,
    secondary = SkyBlue,
    onSecondary = Color(0xFF002B45),
    secondaryContainer = Color(0xFF00394F),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = MintGreen,
    onTertiary = Color(0xFF003820),
    tertiaryContainer = Color(0xFF004D30),
    onTertiaryContainer = Color(0xFF9DFAC8),
    error = AccentRed,
    background = SurfaceDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFF1E2D1E),
    onSurfaceVariant = OnSurfaceMuted,
    outline = GlassBorder
)

@Composable
fun NourishCycleTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Use safe cast — context is not always an Activity in Compose Previews (R3 fix)
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = NourishColorScheme,
        typography = AppTypography,
        content = content
    )
}
