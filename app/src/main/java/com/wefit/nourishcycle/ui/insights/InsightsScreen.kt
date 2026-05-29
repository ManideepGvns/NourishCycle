package com.wefit.nourishcycle.ui.insights

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.*   // rememberBottom / rememberStart extensions
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.common.Fill             // ARGB data class: Fill(color.toArgb())
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.wefit.nourishcycle.ui.components.DonutChart
import com.wefit.nourishcycle.ui.components.GlassTile
import com.wefit.nourishcycle.ui.components.TrendChip
import com.wefit.nourishcycle.ui.theme.AccentAmber
import com.wefit.nourishcycle.ui.theme.GradientEnd
import com.wefit.nourishcycle.ui.theme.GradientMid
import com.wefit.nourishcycle.ui.theme.GradientStart
import com.wefit.nourishcycle.ui.theme.LimeGreen
import com.wefit.nourishcycle.ui.theme.LimeGreenLight
import com.wefit.nourishcycle.ui.theme.OnSurface
import com.wefit.nourishcycle.viewmodel.BestDayState
import com.wefit.nourishcycle.viewmodel.InsightsUiState
import com.wefit.nourishcycle.viewmodel.InsightsViewModel
import com.wefit.nourishcycle.viewmodel.StreakState
import com.wefit.nourishcycle.viewmodel.TrendState

@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: InsightsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Predictive back: mirrors entry (slide-up) in reverse (slide-down)
    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize()) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            when (val state = uiState) {
                is InsightsUiState.Loading -> LoadingContent()
                is InsightsUiState.InsufficientData -> InsufficientDataContent()
                is InsightsUiState.Error -> ErrorContent(state.message)
                is InsightsUiState.Ready -> ReadyContent(state)
            }
        }
    }
}

@Composable
private fun ReadyContent(state: InsightsUiState.Ready) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── KPI Cards ────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total weekly %
            KpiCard(
                label = "Weekly",
                value = "${state.totalWeekPct}%",
                trend = state.trend,
                modifier = Modifier.weight(1f)
            )
            // Streak
            StreakCard(state.streak, Modifier.weight(1f))
            // Best day
            BestDayCard(state.bestDay, Modifier.weight(1f))
        }

        // ── Bar Chart ────────────────────────────────────────────────
        GlassTile(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            contentPadding = 16.dp
        ) {
            Column {
                Text(
                    "Daily Completion",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface
                )
                Spacer(Modifier.height(12.dp))
                // key(dataHash) ensures Vico re-animates when bar heights change
                key(state.dataHash) {
                    NourishBarChart(
                        dailyCompletions = state.dailyCompletions.map { it.pct.toFloat() },
                        dayLabels = state.dailyCompletions.map { it.dayName.take(3) }
                    )
                }
            }
        }

        // ── Donut Chart ──────────────────────────────────────────────
        GlassTile(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            contentPadding = 16.dp
        ) {
            Column {
                Text(
                    "Meal Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface
                )
                Spacer(Modifier.height(12.dp))
                DonutChart(
                    categoryCompletions = state.categoryCompletions,
                    overallPct = state.totalWeekPct
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NourishBarChart(
    dailyCompletions: List<Float>,
    dayLabels: List<String>
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyCompletions) {
        modelProducer.runTransaction {
            columnSeries { series(dailyCompletions) }
        }
    }

    val columnComponent = rememberLineComponent(
        fill = Fill(LimeGreen.toArgb()),
        thickness = 20.dp,
        shape = CorneredShape.rounded(allPercent = 40)
    )

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        dayLabels.getOrElse(value.toInt()) { "" }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(columnComponent)
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter)
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    trend: TrendState,
    modifier: Modifier = Modifier
) {
    GlassTile(modifier = modifier, cornerRadius = 16.dp, contentPadding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            Spacer(Modifier.height(4.dp))
            TrendChip(trend)
        }
    }
}

@Composable
private fun StreakCard(streak: StreakState, modifier: Modifier = Modifier) {
    val animatedCount by animateIntAsState(streak.count, tween(500), label = "streakCount")
    val infiniteTransition = rememberInfiniteTransition(label = "streakDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "dotAlpha"
    )

    GlassTile(modifier = modifier, cornerRadius = 16.dp, contentPadding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Streak", style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$animatedCount days",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                if (streak.todayIsActive) {
                    Spacer(Modifier.size(6.dp))
                    Canvas(Modifier.size(8.dp)) {
                        drawCircle(AccentAmber.copy(alpha = dotAlpha))
                    }
                }
            }
        }
    }
}

@Composable
private fun BestDayCard(bestDay: BestDayState, modifier: Modifier = Modifier) {
    GlassTile(modifier = modifier, cornerRadius = 16.dp, contentPadding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Best Day", style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            // String — use AnimatedContent (NOT animateIntAsState)
            AnimatedContent(
                targetState = bestDay.dayName ?: "--",
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "bestDay"
            ) { name ->
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
            }
            if (bestDay.dayName != null) {
                Text(
                    "${bestDay.pct}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimeGreen
                )
            }
        }
    }
}

@Composable
private fun InsufficientDataContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyBounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "bounce"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Eco,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { translationY = offsetY },
            tint = LimeGreenLight
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Keep going!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Complete meals across multiple days to unlock your personalised insights dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Loading insights…", color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
    }
}
