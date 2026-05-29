package com.wefit.nourishcycle.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wefit.nourishcycle.ui.theme.GradientEnd
import com.wefit.nourishcycle.ui.theme.GradientMid
import com.wefit.nourishcycle.ui.theme.GradientStart
import com.wefit.nourishcycle.ui.theme.LimeGreen
import com.wefit.nourishcycle.ui.theme.LimeGreenLight
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {

    // Leaf draw progress: 0f → 1f over 1200ms
    val leafProgress = remember { Animatable(0f) }
    // App name: slides up from +60dp → 0dp
    val nameOffsetY = remember { Animatable(60f) }
    val nameAlpha = remember { Animatable(0f) }
    // Tagline
    val taglineAlpha = remember { Animatable(0f) }

    // Particles: 8 dots with staggered radial animation
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particleRadius by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleRadius"
    )
    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleAlpha"
    )

    LaunchedEffect(Unit) {
        leafProgress.animateTo(1f, tween(1200))
        // Stagger: name slides up after leaf is 60% drawn
        nameOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
        nameAlpha.animateTo(1f, tween(500))
        delay(200)
        taglineAlpha.animateTo(1f, tween(600))
        delay(800)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background gradient
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(GradientStart, GradientMid, GradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }

        // Particle dots around the leaf icon
        Canvas(Modifier.size(300.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            repeat(8) { i ->
                val angle = (i * 45f) * (Math.PI / 180f).toFloat()
                val x = cx + particleRadius * cos(angle.toDouble()).toFloat()
                val y = cy + particleRadius * sin(angle.toDouble()).toFloat()
                drawCircle(
                    color = LimeGreenLight.copy(alpha = particleAlpha),
                    radius = (4f + i % 3 * 2f),
                    center = Offset(x, y)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated leaf icon
            Canvas(Modifier.size(120.dp)) {
                val w = size.width
                val h = size.height
                val progress = leafProgress.value

                // Draw leaf outline path progressively
                val leafPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f)
                    cubicTo(
                        w * 0.85f, h * 0.2f,
                        w * 0.85f, h * 0.6f,
                        w * 0.5f, h * 0.85f
                    )
                    cubicTo(
                        w * 0.15f, h * 0.6f,
                        w * 0.15f, h * 0.2f,
                        w * 0.5f, h * 0.1f
                    )
                    close()
                }

                // Filled leaf (appears as progress increases)
                drawPath(
                    path = leafPath,
                    brush = Brush.linearGradient(
                        colors = listOf(LimeGreen, LimeGreenLight),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    alpha = progress
                )

                // Leaf outline stroke
                drawPath(
                    path = leafPath,
                    color = Color.White.copy(alpha = 0.6f * progress),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Stem
                if (progress > 0.5f) {
                    val stemAlpha = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)
                    drawLine(
                        color = Color.White.copy(alpha = stemAlpha),
                        start = Offset(w * 0.5f, h * 0.85f),
                        end = Offset(w * 0.5f, h * 0.97f),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Left sprout
                    drawLine(
                        color = Color.White.copy(alpha = stemAlpha * 0.8f),
                        start = Offset(w * 0.5f, h * 0.91f),
                        end = Offset(w * 0.36f, h * 0.83f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Right sprout
                    drawLine(
                        color = Color.White.copy(alpha = stemAlpha * 0.8f),
                        start = Offset(w * 0.5f, h * 0.93f),
                        end = Offset(w * 0.64f, h * 0.85f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Center vein
                    drawLine(
                        color = GradientStart.copy(alpha = stemAlpha * 0.5f),
                        start = Offset(w * 0.5f, h * 0.15f),
                        end = Offset(w * 0.5f, h * 0.82f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // App name
            Text(
                text = "NourishCycle",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .offset(y = nameOffsetY.value.dp)
                    .alpha(nameAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your Fertility Wellness Journey",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
