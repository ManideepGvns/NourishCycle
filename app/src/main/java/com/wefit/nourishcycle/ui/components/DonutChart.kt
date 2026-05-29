package com.wefit.nourishcycle.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wefit.nourishcycle.data.MealCategory
import com.wefit.nourishcycle.ui.theme.AccentGrey
import com.wefit.nourishcycle.ui.theme.OnSurface
import com.wefit.nourishcycle.ui.theme.SegmentColors

private val easeOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

// Each of the 7 meal-slot segments occupies an equal 360/7 ≈ 51.4° slice of the donut.
// Within that slice, the coloured arc = slotCompletionRate × sliceAngle.
// The grey background arc fills the rest of the slice.
// Total arc is always exactly 360°, so the chart is geometrically correct (L5 fix).
private const val SEGMENTS = 7
private const val FULL_ANGLE = 360f
private const val SLICE_ANGLE = FULL_ANGLE / SEGMENTS  // ≈ 51.43°
private const val GAP = 4f                              // degrees gap between slices

/**
 * Donut chart with 7 fixed-angle segments — one per meal slot.
 *
 * Key rules:
 *  1. All 7 animateFloatAsState calls are ALWAYS made regardless of data nullity
 *     (Compose composition law: no conditional state calls).
 *  2. When all fill fractions are ~0 a neutral grey ring is drawn.
 *  3. Segment fills are clamped [0f, 1f] so the coloured arc ≤ the grey background arc.
 *  4. Total arc = 360° always — geometrically correct (L5 fix from previous bug where
 *     sweep = fraction × 360° per segment could overflow 360° combined).
 */
@Composable
fun DonutChart(
    categoryCompletions: List<Float>?,  // 7 floats [0f..1f]; null during loading
    overallPct: Int,
    modifier: Modifier = Modifier
) {
    // Always 7 animateFloatAsState calls — targets are 0f when data is null (composition law)
    val fill0 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(0) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f0"
    )
    val fill1 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(1) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f1"
    )
    val fill2 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(2) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f2"
    )
    val fill3 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(3) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f3"
    )
    val fill4 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(4) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f4"
    )
    val fill5 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(5) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f5"
    )
    val fill6 by animateFloatAsState(
        targetValue = (categoryCompletions?.getOrElse(6) { 0f } ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = easeOutCubic), label = "f6"
    )
    val fills = listOf(fill0, fill1, fill2, fill3, fill4, fill5, fill6)

    val animatedPct by animateIntAsState(
        targetValue = overallPct,
        animationSpec = tween(600),
        label = "overallPct"
    )

    val allZero = fills.all { it < 0.01f }
    val strokeWidth = 28.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(Modifier.size(160.dp)) {
                val strokePx = strokeWidth.toPx()
                val stroke = Stroke(width = strokePx, cap = StrokeCap.Butt)
                val inset = strokePx / 2f
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)

                if (allZero) {
                    // No data: draw a neutral grey ring
                    drawArc(
                        color = AccentGrey.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                } else {
                    fills.forEachIndexed { i, fillFraction ->
                        val sliceStart = -90f + i * SLICE_ANGLE + GAP / 2f
                        val usableArc = SLICE_ANGLE - GAP

                        // Grey background track for this slice
                        drawArc(
                            color = AccentGrey.copy(alpha = 0.25f),
                            startAngle = sliceStart,
                            sweepAngle = usableArc,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )

                        // Coloured fill proportional to completion rate
                        val coloredArc = usableArc * fillFraction
                        if (coloredArc > 0.5f) {
                            drawArc(
                                color = SegmentColors[i],
                                startAngle = sliceStart,
                                sweepAngle = coloredArc,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = stroke
                            )
                        }
                    }
                }
            }

            // Center label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$animatedPct%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = "done",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.width(20.dp))

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MealCategory.entries.forEach { cat ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(10.dp)) {
                        drawCircle(color = SegmentColors[cat.slotIndex])
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = cat.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
