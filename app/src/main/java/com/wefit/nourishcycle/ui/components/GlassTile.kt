package com.wefit.nourishcycle.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wefit.nourishcycle.ui.theme.GlassBorder
import com.wefit.nourishcycle.ui.theme.GlassInnerHighlight
import com.wefit.nourishcycle.ui.theme.GlassWhiteHigh
import com.wefit.nourishcycle.ui.theme.GlassWhiteLow

/**
 * Glass morphism tile — works on all API levels (no blur API required).
 * Achieves the glass look via:
 *   1. Semi-transparent white gradient background
 *   2. 1dp white border
 *   3. Top-left inner highlight drawn on Canvas
 */
@Composable
fun GlassTile(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                val cornerPx = cornerRadius.toPx()

                // Layer 1: semi-transparent gradient fill
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(GlassWhiteHigh, GlassWhiteLow),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(cornerPx)
                )

                // Layer 2: top-left inner highlight — simulates light reflection
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassInnerHighlight,
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.5f, size.height * 0.5f)
                    ),
                    cornerRadius = CornerRadius(cornerPx),
                    style = Stroke(width = 12.dp.toPx())
                )

                // Layer 3: subtle bottom-right shadow for depth
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f)
                        ),
                        start = Offset(size.width * 0.5f, size.height * 0.5f),
                        end = Offset(size.width, size.height)
                    ),
                    topLeft = Offset(size.width * 0.3f, size.height * 0.3f),
                    size = Size(size.width * 0.7f, size.height * 0.7f),
                    cornerRadius = CornerRadius(cornerPx)
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorder,
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}
