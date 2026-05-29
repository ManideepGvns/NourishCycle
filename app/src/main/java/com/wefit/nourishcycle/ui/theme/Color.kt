package com.wefit.nourishcycle.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette — vibrant lime → sky → teal
val LimeGreen = Color(0xFF76C442)
val LimeGreenLight = Color(0xFFA5D86B)
val SkyBlue = Color(0xFF29B6F6)
val DeepTeal = Color(0xFF00838F)
val MintGreen = Color(0xFF4CAF82)

// Background gradient stops
val GradientStart = Color(0xFF1A6B2A)   // deep forest green
val GradientMid = Color(0xFF0D6B8A)    // ocean blue
val GradientEnd = Color(0xFF004D5C)    // deep teal

// Glass tile
val GlassWhiteHigh = Color(0x38FFFFFF)  // alpha ~22%
val GlassWhiteLow = Color(0x15FFFFFF)   // alpha ~8%
val GlassBorder = Color(0x59FFFFFF)     // alpha ~35%
val GlassInnerHighlight = Color(0x26FFFFFF) // top-left inner highlight

// Accent colours
val AccentAmber = Color(0xFFFFA726)
val AccentRed = Color(0xFFEF5350)
val AccentGreen = Color(0xFF66BB6A)
val AccentGrey = Color(0xFF78909C)

// Donut segment colours (7 distinct, material palette)
val SegmentMorningDrink = Color(0xFF80CBC4)   // teal 200
val SegmentBreakfast = Color(0xFFA5D6A7)      // green 200
val SegmentMidMorning = Color(0xFFFFCC80)     // orange 200
val SegmentLunch = Color(0xFF90CAF9)          // blue 200
val SegmentEveningSnack = Color(0xFFCE93D8)   // purple 200
val SegmentDinner = Color(0xFFEF9A9A)         // red 200
val SegmentNightDrink = Color(0xFFFFF59D)     // yellow 200

val SegmentColors = listOf(
    SegmentMorningDrink,
    SegmentBreakfast,
    SegmentMidMorning,
    SegmentLunch,
    SegmentEveningSnack,
    SegmentDinner,
    SegmentNightDrink
)

// Surface
val SurfaceDark = Color(0xFF0A1628)
val OnSurface = Color(0xFFECF7EC)
val OnSurfaceMuted = Color(0xB3ECFFF0)
