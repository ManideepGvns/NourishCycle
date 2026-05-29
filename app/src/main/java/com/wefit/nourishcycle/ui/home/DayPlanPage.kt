package com.wefit.nourishcycle.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import java.time.LocalDate

@Composable
fun DayPlanPage(
    pageIndex: Int,
    dayPlanIndex: Int,
    dateStr: String,
    completedSlots: Set<Int>,
    onToggle: (slotIndex: Int) -> Unit,
    isLoading: Boolean,
    isSettled: Boolean,
    visitedPages: MutableSet<Int>,
    modifier: Modifier = Modifier
) {
    val dayPlan = DietPlanData.days[dayPlanIndex]
    val slots = dayPlan.slots

    // Determine if this page is a future date — ticking is disabled for future days
    val isFutureDay = remember(dateStr) {
        if (dateStr.length == 10) {
            runCatching { LocalDate.parse(dateStr).isAfter(LocalDate.now()) }.getOrElse { false }
        } else false
    }

    // Per-slot visibility state — stagger only on first visit
    val visible = remember { mutableStateListOf(*Array(slots.size) { false }) }

    LaunchedEffect(pageIndex, isSettled) {
        if (isSettled && pageIndex !in visitedPages) {
            visitedPages.add(pageIndex)
            for (i in visible.indices) visible[i] = false
            slots.forEachIndexed { index, _ ->
                delay(index * 45L)
                if (index < visible.size) visible[index] = true
            }
        } else if (pageIndex in visitedPages) {
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
            .padding(horizontal = 8.dp)
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
                // Clickable wraps the card content — single tap target, no nested clickables.
                // Future days are locked: clickable is disabled and card shows muted appearance.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !isFutureDay,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onToggle(slot.slotIndex) }
                ) {
                    MealSlotCard(
                        slot = slot,
                        isCompleted = completedSlots.contains(slot.slotIndex),
                        isLocked = isFutureDay
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        val completedCount = completedSlots.size
        val total = slots.size
        if (completedCount > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$completedCount / $total meals completed ✓",
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
