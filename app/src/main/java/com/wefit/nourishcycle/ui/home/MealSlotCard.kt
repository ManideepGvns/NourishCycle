package com.wefit.nourishcycle.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.wefit.nourishcycle.data.MealSlot
import com.wefit.nourishcycle.ui.components.AnimatedCheckbox
import com.wefit.nourishcycle.ui.components.GlassTile
import com.wefit.nourishcycle.ui.theme.AccentAmber
import com.wefit.nourishcycle.ui.theme.LimeGreen
import com.wefit.nourishcycle.ui.theme.OnSurface

@Composable
fun MealSlotCard(
    slot: MealSlot,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.5f else 1f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )

    GlassTile(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        contentPadding = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Time badge pill
                Surface(
                    color = AccentAmber.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = slot.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Slot label (e.g. "Breakfast")
                Text(
                    text = slot.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface.copy(alpha = contentAlpha),
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )

                // Animated checkbox
                AnimatedCheckbox(
                    isChecked = isCompleted,
                    onToggle = onToggle
                )
            }

            if (slot.options.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                slot.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(contentAlpha)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = LimeGreen,
                            modifier = Modifier.padding(end = 6.dp, top = 1.dp)
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                }
            }

            if (slot.tip.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "💡 ${slot.tip}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimeGreen.copy(alpha = 0.8f * contentAlpha),
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        }
    }
}
