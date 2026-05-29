package com.wefit.nourishcycle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.wefit.nourishcycle.ui.theme.AccentGreen
import com.wefit.nourishcycle.ui.theme.GlassBorder
import kotlinx.coroutines.launch

/**
 * Visual-only animated checkbox. Click handling is on the parent card,
 * not here — having two overlapping clickable modifiers caused double-toggles.
 */
@Composable
fun AnimatedCheckbox(
    isChecked: Boolean,
    modifier: Modifier = Modifier
) {
    // Scale animation: bouncy on check, gentle on uncheck
    val scale = remember { Animatable(1f) }
    // Checkmark stroke progress: 0f = not drawn, 1f = fully drawn
    val checkProgress = remember { Animatable(if (isChecked) 1f else 0f) }
    // Fill alpha: 0f = empty, 1f = filled
    val fillAlpha = remember { Animatable(if (isChecked) 1f else 0f) }

    LaunchedEffect(isChecked) {
        if (isChecked) {
            // Bouncy scale up then back
            launch {
                scale.animateTo(
                    1.25f,
                    spring(dampingRatio = 0.4f, stiffness = 600f)
                )
                scale.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 500f))
            }
            // Draw checkmark stroke
            launch { checkProgress.animateTo(1f, tween(300)) }
            // Fill the box
            launch { fillAlpha.animateTo(1f, tween(200)) }
        } else {
            // Gentle uncheck: slight squeeze
            launch {
                scale.animateTo(0.9f, tween(100))
                scale.animateTo(1f, tween(100))
            }
            launch { checkProgress.animateTo(0f, tween(150)) }
            launch { fillAlpha.animateTo(0f, tween(200)) }
        }
    }

    Canvas(
        modifier = modifier
            .size(28.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        val cornerRadius = CornerRadius(6.dp.toPx())
        val strokeWidth = 2.dp.toPx()

        // Box fill (green when checked)
        drawRoundRect(
            color = AccentGreen.copy(alpha = fillAlpha.value),
            cornerRadius = cornerRadius
        )

        // Box border (always visible)
        drawRoundRect(
            color = if (isChecked) AccentGreen else GlassBorder,
            cornerRadius = cornerRadius,
            style = Stroke(width = strokeWidth)
        )

        // Checkmark path: drawn progressively via checkProgress
        if (checkProgress.value > 0f) {
            val w = size.width
            val h = size.height

            // Checkmark: short left leg then long right leg
            val p1 = Offset(w * 0.2f, h * 0.52f)
            val p2 = Offset(w * 0.42f, h * 0.72f)
            val p3 = Offset(w * 0.8f, h * 0.3f)

            val totalLength = dist(p1, p2) + dist(p2, p3)
            val drawn = checkProgress.value * totalLength

            val checkPath = Path()
            checkPath.moveTo(p1.x, p1.y)

            val leg1Length = dist(p1, p2)
            if (drawn <= leg1Length) {
                // Still drawing first segment
                val t = drawn / leg1Length
                checkPath.lineTo(lerp(p1.x, p2.x, t), lerp(p1.y, p2.y, t))
            } else {
                // First segment complete, draw into second
                checkPath.lineTo(p2.x, p2.y)
                val remainingDraw = drawn - leg1Length
                val leg2Length = dist(p2, p3)
                val t = (remainingDraw / leg2Length).coerceIn(0f, 1f)
                checkPath.lineTo(lerp(p2.x, p3.x, t), lerp(p2.y, p3.y, t))
            }

            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun dist(a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
