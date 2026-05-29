package com.wefit.nourishcycle.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wefit.nourishcycle.data.DietPlanData
import com.wefit.nourishcycle.ui.components.DayPageSkeleton
import kotlinx.coroutines.delay

@Composable
fun DayPlanPage(
    pageIndex: Int,         // 0–6, position in the HorizontalPager
    dayPlanIndex: Int,      // 0–6, index into DietPlanData.days
    dateStr: String,
    completedSlots: Set<Int>,
    onToggle: (slotIndex: Int) -> Unit,
    isLoading: Boolean,
    isSettled: Boolean,     // true when pagerState.settledPage == pageIndex
    visitedPages: MutableSet<Int>,
    modifier: Modifier = Modifier
) {
    val dayPlan = DietPlanData.days[dayPlanIndex]
    val slots = dayPlan.slots

    // Per-slot visibility state — stagger only on first visit
    val visible = remember { mutableStateListOf(*Array(slots.size) { false }) }

    LaunchedEffect(pageIndex, isSettled) {
        if (isSettled && pageIndex !in visitedPages) {
            visitedPages.add(pageIndex)
            // Reset visibility for fresh stagger
            for (i in visible.indices) visible[i] = false
            slots.forEachIndexed { index, _ ->
                delay(index * 45L)
                if (index < visible.size) visible[index] = true
            }
        } else if (pageIndex in visitedPages) {
            // Previously visited: show all immediately
            for (i in visible.indices) visible[i] = true
        }
    }

    if (isLoading) {
        DayPageSkeleton()
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        slots.forEachIndexed { index, slot ->
            AnimatedVisibility(
                visible = visible.getOrElse(index) { false },
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            ) {
                Column {
                    MealSlotCard(
                        slot = slot,
                        isCompleted = completedSlots.contains(slot.slotIndex),
                        onToggle = { onToggle(slot.slotIndex) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Progress footer
        val completedCount = completedSlots.size
        val total = slots.size
        if (completedCount > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$completedCount / $total meals completed today ✓",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
}
