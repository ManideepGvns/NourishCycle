package com.wefit.nourishcycle.ui.insights

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val mealLabels = listOf("🌅", "🍳", "🥗", "🍱", "🥙", "🍽️", "🌙")
    val dayAbbrevs = state.dailyCompletions.map { it.dayName.take(3) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Row 1: Weekly | Streak | Best Day ────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiCard("Weekly", "${state.totalWeekPct}%", state.trend, Modifier.weight(1f))
            StreakCard(state.streak, Modifier.weight(1f))
            BestDayCard(state.bestDay, Modifier.weight(1f))
        }

        // ── Row 2: Today's Progress | Consistency ────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TodayProgressCard(state.todayCompleted, state.todayPct, Modifier.weight(1f))
            ConsistencyCard(state.consistencyScore, Modifier.weight(1f))
        }

        // ── Bar Chart — Daily Completion ─────────────────────────────
        GlassTile(Modifier.fillMaxWidth(), cornerRadius = 20.dp, contentPadding = 16.dp) {
            Column {
                Text("Daily Completion", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                Spacer(Modifier.height(12.dp))
                key(state.dataHash) {
                    NourishBarChart(
                        dailyCompletions = state.dailyCompletions.map { it.pct.toFloat() },
                        dayLabels = dayAbbrevs
                    )
                }
            }
        }

        // ── Meal Heatmap ──────────────────────────────────────────────
        GlassTile(Modifier.fillMaxWidth(), cornerRadius = 20.dp, contentPadding = 16.dp) {
            Column {
                Text("Meal Heatmap", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                Text(
                    "Each row = a day · Each cell = a meal slot",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(12.dp))
                MealHeatmap(
                    heatmap = state.mealHeatmap,
                    dayLabels = dayAbbrevs,
                    mealLabels = mealLabels,
                    isFutureDay = state.dailyCompletions.map { it.isFuture }
                )
            }
        }

        // ── Best & Worst Meal ─────────────────────────────────────────
        if (state.bestSlotIndex >= 0) {
            BestWorstMealCard(
                mealSlotRates = state.mealSlotRates,
                mealLabels = mealLabels,
                bestSlotIndex = state.bestSlotIndex,
                worstSlotIndex = state.worstSlotIndex
            )
        }

        // ── Donut Chart — Meal Category Breakdown ────────────────────
        GlassTile(Modifier.fillMaxWidth(), cornerRadius = 20.dp, contentPadding = 16.dp) {
            Column {
                Text("Meal Category Breakdown", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                Spacer(Modifier.height(12.dp))
                DonutChart(categoryCompletions = state.categoryCompletions, overallPct = state.totalWeekPct)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Today's Progress card ──────────────────────────────────────────────────

@Composable
private fun TodayProgressCard(completed: Int, pct: Int, modifier: Modifier = Modifier) {
    val animatedPct by animateFloatAsState(
        targetValue = pct / 100f,
        animationSpec = tween(800),
        label = "todayProgress"
    )
    GlassTile(modifier = modifier, cornerRadius = 16.dp, contentPadding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Today", style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(
                "$completed / 7",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedPct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = LimeGreen,
                trackColor = LimeGreen.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$pct%",
                style = MaterialTheme.typography.labelSmall,
                color = LimeGreen
            )
        }
    }
}

// ── Consistency Score card ─────────────────────────────────────────────────

@Composable
private fun ConsistencyCard(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateIntAsState(score, tween(700), label = "consistency")
    val emoji = when {
        score >= 80 -> "🌟"
        score >= 60 -> "💪"
        score >= 40 -> "🌱"
        else -> "🎯"
    }
    GlassTile(modifier = modifier, cornerRadius = 16.dp, contentPadding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Consistency", style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(
                "$animatedScore%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = when {
                    score >= 70 -> LimeGreen
                    score >= 40 -> AccentAmber
                    else -> Color(0xFFFF6B6B)
                }
            )
            Spacer(Modifier.height(2.dp))
            Text(emoji, fontSize = 16.sp)
        }
    }
}

// ── Meal Heatmap ───────────────────────────────────────────────────────────

@Composable
private fun MealHeatmap(
    heatmap: List<List<Boolean>>,
    dayLabels: List<String>,
    mealLabels: List<String>,
    isFutureDay: List<Boolean>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header row — meal slot icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(Modifier.width(28.dp)) // align with day labels
            mealLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = OnSurface.copy(alpha = 0.6f)
                )
            }
        }
        // One row per day
        heatmap.forEachIndexed { dayIndex, slots ->
            val isFuture = isFutureDay.getOrElse(dayIndex) { false }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayLabels.getOrElse(dayIndex) { "" },
                    fontSize = 9.sp,
                    color = OnSurface.copy(alpha = 0.5f),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End
                )
                slots.forEach { done ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isFuture -> Color.White.copy(alpha = 0.08f)
                                    done     -> LimeGreen.copy(alpha = 0.85f)
                                    else     -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }
        }
        // Legend
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(LimeGreen.copy(alpha = 0.85f), "Completed")
            LegendDot(Color.White.copy(alpha = 0.15f), "Missed")
            LegendDot(Color.White.copy(alpha = 0.08f), "Future")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.6f))
    }
}

// ── Best & Worst Meal card ─────────────────────────────────────────────────

@Composable
private fun BestWorstMealCard(
    mealSlotRates: List<Float>,
    mealLabels: List<String>,
    bestSlotIndex: Int,
    worstSlotIndex: Int
) {
    val mealNames = listOf(
        "Morning Drink", "Breakfast", "Mid-Morning Snack",
        "Lunch", "Evening Snack", "Dinner", "Night Drink"
    )
    GlassTile(Modifier.fillMaxWidth(), cornerRadius = 20.dp, contentPadding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Meal Performance", style = MaterialTheme.typography.titleMedium, color = OnSurface)

            // All slots as a horizontal bar chart
            mealSlotRates.forEachIndexed { index, rate ->
                val animatedRate by animateFloatAsState(rate, tween(800), label = "mealRate$index")
                val isBest = index == bestSlotIndex
                val isWorst = index == worstSlotIndex
                val barColor = when {
                    isBest  -> LimeGreen
                    isWorst -> Color(0xFFFF6B6B)
                    else    -> AccentAmber.copy(alpha = 0.7f)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(mealLabels.getOrElse(index) { "" }, fontSize = 14.sp, modifier = Modifier.width(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            mealNames.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurface.copy(alpha = 0.75f)
                        )
                        Spacer(Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { animatedRate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = barColor,
                            trackColor = barColor.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                    Text(
                        "${(rate * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = barColor,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End
                    )
                    if (isBest) Icon(Icons.Rounded.EmojiEvents, null, tint = LimeGreen, modifier = Modifier.size(14.dp))
                    if (isWorst) Icon(Icons.Rounded.Warning, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(14.dp))
                }
            }
        }
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
