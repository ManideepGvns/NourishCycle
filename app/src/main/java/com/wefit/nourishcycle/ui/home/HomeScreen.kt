package com.wefit.nourishcycle.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wefit.nourishcycle.data.DietPlanData
import com.wefit.nourishcycle.ui.theme.GradientEnd
import com.wefit.nourishcycle.ui.theme.GradientMid
import com.wefit.nourishcycle.ui.theme.GradientStart
import com.wefit.nourishcycle.ui.theme.LimeGreen
import com.wefit.nourishcycle.ui.theme.LimeGreenLight
import com.wefit.nourishcycle.ui.theme.OnSurface
import com.wefit.nourishcycle.viewmodel.HomeViewModel
import com.wefit.nourishcycle.viewmodel.Greeting
import com.wefit.nourishcycle.viewmodel.computeGreeting
import com.wefit.nourishcycle.viewmodel.toDisplayString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    onNavigateToInsights: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Greeting: refreshed every 60 seconds to catch time boundary crossings
    val greeting by produceState(initialValue = computeGreeting()) {
        while (true) {
            delay(60_000L)
            value = computeGreeting()
        }
    }

    // Pager state — single source of truth for chips and pager
    val pagerState = rememberPagerState(
        initialPage = uiState.todayPageIndex,
        pageCount = { 7 }
    )

    // Sync pager to todayPageIndex when it loads asynchronously (L3 fix):
    // initialPage is only read once, so we need a LaunchedEffect to scroll after data loads.
    LaunchedEffect(uiState.todayPageIndex) {
        if (!uiState.isLoading) {
            pagerState.scrollToPage(uiState.todayPageIndex)
        }
    }

    // Track which pages have already been stagger-animated
    val visitedPages = remember { mutableSetOf<Int>() }

    // FAB pulse: Animatable loop (can be called from coroutine, unlike animateFloatAsState)
    val fabScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            fabScale.animateTo(1.12f, spring(stiffness = 500f))
            fabScale.animateTo(1f, spring(stiffness = 500f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Background gradient ──────────────────────────────────────
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
        ) {
            // ── Top bar ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                AnimatedContent(
                    targetState = greeting,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn(tween(300))) togetherWith
                                (slideOutVertically { -it } + fadeOut(tween(300)))
                    },
                    label = "greeting"
                ) { currentGreeting ->
                    Column {
                        Text(
                            text = currentGreeting.toDisplayString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Let's nourish your body today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ── Day chip selector ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DietPlanData.days.forEachIndexed { index, day ->
                    val isSelected = pagerState.currentPage == index ||
                            (pagerState.isScrollInProgress && pagerState.targetPage == index)
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) LimeGreen else Color.Transparent,
                        animationSpec = spring(stiffness = 400f),
                        label = "chipBorder$index"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) LimeGreen else OnSurface.copy(alpha = 0.7f),
                        animationSpec = spring(stiffness = 400f),
                        label = "chipText$index"
                    )
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) LimeGreen.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.1f),
                        animationSpec = spring(stiffness = 400f),
                        label = "chipBg$index"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(chipBg)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.dayName.take(3),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = textColor
                            )
                            // Active underline drawn inside chip
                            Box(
                                Modifier
                                    .padding(top = 3.dp)
                                    .width(16.dp)
                                    .height(2.dp)
                                    .background(borderColor, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Day pager — dock-magnification style ─────────────────
            // contentPadding exposes ~56dp of the neighbour pages on each side.
            // pageSpacing adds breathing room between cards.
            // graphicsLayer scales the current page to 1f and neighbours to 0.84f,
            // interpolated smoothly as the user drags — similar to macOS Dock magnification.
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 52.dp),
                pageSpacing = 12.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val dateStr = uiState.cycleDates.getOrElse(pageIndex) { "" }
                val dayPlanIndex = if (dateStr.isNotEmpty()) {
                    viewModel.dayPlanIndex(dateStr)
                } else pageIndex % 7
                val completedSlots = uiState.completions[dateStr] ?: emptySet()
                val isSettled = pagerState.settledPage == pageIndex

                // Fractional offset from the currently settled page (0 = this page is focused)
                val pageOffset = ((pagerState.currentPage - pageIndex).toFloat() +
                        pagerState.currentPageOffsetFraction).absoluteValue

                val scale = lerp(start = 0.84f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                val contentAlpha = lerp(start = 0.55f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))

                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = contentAlpha
                    }
                ) {
                    DayPlanPage(
                        pageIndex = pageIndex,
                        dayPlanIndex = dayPlanIndex,
                        dateStr = dateStr,
                        completedSlots = completedSlots,
                        onToggle = { slotIndex -> viewModel.toggleSlot(dateStr, slotIndex) },
                        isLoading = uiState.isLoading,
                        isSettled = isSettled,
                        visitedPages = visitedPages
                    )
                }
            }
        }

        // ── Insights FAB ─────────────────────────────────────────────
        FloatingActionButton(
            onClick = onNavigateToInsights,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp)
                .graphicsLayer {
                    scaleX = fabScale.value
                    scaleY = fabScale.value
                },
            containerColor = LimeGreen,
            contentColor = Color(0xFF003300),
            shape = CircleShape
        ) {
            Icon(Icons.Rounded.BarChart, contentDescription = "View Insights")
        }
    }

}
