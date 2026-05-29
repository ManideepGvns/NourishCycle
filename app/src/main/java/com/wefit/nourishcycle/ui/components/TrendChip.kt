package com.wefit.nourishcycle.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wefit.nourishcycle.ui.theme.AccentGreen
import com.wefit.nourishcycle.ui.theme.AccentGrey
import com.wefit.nourishcycle.ui.theme.AccentRed
import com.wefit.nourishcycle.viewmodel.TrendState

@Composable
fun TrendChip(trend: TrendState, modifier: Modifier = Modifier) {
    // Insufficient renders as nothing (takes no space)
    if (trend is TrendState.Insufficient) return

    AnimatedContent(
        targetState = trend,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "trendChip"
    ) { state ->
        when (state) {
            is TrendState.Uptrend -> TrendPill(
                label = "+${state.delta}%",
                icon = { Icon(Icons.Rounded.TrendingUp, null, Modifier.size(14.dp), tint = AccentGreen) },
                color = AccentGreen.copy(alpha = 0.2f),
                textColor = AccentGreen,
                modifier = modifier
            )
            is TrendState.Downtrend -> TrendPill(
                label = "-${state.delta}%",
                icon = { Icon(Icons.Rounded.TrendingDown, null, Modifier.size(14.dp), tint = AccentRed) },
                color = AccentRed.copy(alpha = 0.2f),
                textColor = AccentRed,
                modifier = modifier
            )
            is TrendState.Flat -> TrendPill(
                label = "Stable",
                icon = { Icon(Icons.Rounded.Remove, null, Modifier.size(14.dp), tint = AccentGrey) },
                color = AccentGrey.copy(alpha = 0.2f),
                textColor = AccentGrey,
                modifier = modifier
            )
            else -> {}
        }
    }
}

@Composable
private fun TrendPill(
    label: String,
    icon: @Composable () -> Unit,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
