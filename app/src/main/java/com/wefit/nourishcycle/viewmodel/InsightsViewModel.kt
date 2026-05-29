package com.wefit.nourishcycle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wefit.nourishcycle.data.DietDatabase
import com.wefit.nourishcycle.data.DietPlanData
import com.wefit.nourishcycle.data.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

// ── Data classes for Insights UI ───────────────────────────────────────────

data class DayCompletion(
    val dateStr: String,
    val dayName: String,
    val completedSlots: Int,      // 0–7
    val pct: Int,                 // 0–100
    val isFuture: Boolean
)

data class StreakState(
    val count: Int,
    val todayIsActive: Boolean    // true = today has ≥1 slot done but < 4
)

data class BestDayState(
    val dayName: String?,         // null when < 2 days have any completion
    val pct: Int
)

sealed class TrendState {
    object Insufficient : TrendState()
    object Flat : TrendState()
    data class Uptrend(val delta: Int) : TrendState()
    data class Downtrend(val delta: Int) : TrendState()
}

sealed class InsightsUiState {
    object Loading : InsightsUiState()
    object InsufficientData : InsightsUiState()
    data class Ready(
        val dailyCompletions: List<DayCompletion>,
        val categoryCompletions: List<Float>,  // 7 entries [0f..1f], one per slot index
        val totalWeekPct: Int,
        val streak: StreakState,
        val bestDay: BestDayState,
        val trend: TrendState,
        val dataHash: Int
    ) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}

// ── ViewModel ──────────────────────────────────────────────────────────────

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DietDatabase.getInstance(application)
    private val dao = db.dietDao()
    private val prefs = PreferencesRepository(application)

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val startDate = prefs.cycleStartDate.first()
                val today = LocalDate.now()
                val dates = prefs.cycleWindowDates(startDate, today)
                val todayStr = today.format(PreferencesRepository.ISO_FORMAT)

                dao.getCompletionsForDates(dates).collect { entities ->
                    val completedMap: Map<String, Set<Int>> = entities
                        .filter { it.isCompleted }
                        .groupBy { it.calendarDate }
                        .mapValues { (_, list) -> list.map { it.mealSlotIndex }.toSet() }

                    _uiState.value = buildUiState(
                        startDate, dates, todayStr, completedMap
                    )
                }
            }.onFailure { e ->
                _uiState.value = InsightsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun buildUiState(
        startDate: LocalDate,
        dates: List<String>,
        todayStr: String,
        completedMap: Map<String, Set<Int>>
    ): InsightsUiState {
        val today = LocalDate.now()

        // ── Daily completions ─────────────────────────────────────────
        // Use prefs.dayIndex to get the diet-plan index (0–6) for the actual calendar date
        // so that day labels match the correct diet-plan day, regardless of cycle start (L4 fix).
        val dailyCompletions = dates.map { dateStr ->
            val date = runCatching { LocalDate.parse(dateStr) }.getOrElse { today }
            val isFuture = date.isAfter(today)
            val completed = completedMap[dateStr]?.size ?: 0
            val pct = if (isFuture) 0 else (completed * 100 / 7)
            val dayPlanIdx = prefs.dayIndex(date, startDate)
            DayCompletion(
                dateStr = dateStr,
                dayName = DietPlanData.days[dayPlanIdx].dayName,
                completedSlots = completed,
                pct = pct,
                isFuture = isFuture
            )
        }

        // Check if any past day has completion data
        val daysWithData = dailyCompletions.filter { !it.isFuture && it.completedSlots > 0 }
        if (daysWithData.isEmpty()) return InsightsUiState.InsufficientData

        // ── Category completions (per slot index, 0–6) ───────────────
        // Each slot: fraction of past non-future days that completed it
        val pastDates = dates.zip(dailyCompletions)
            .filter { (_, dc) -> !dc.isFuture }
            .map { (d, _) -> d }
        val pastCount = pastDates.size.coerceAtLeast(1)
        val categoryCompletions = (0..6).map { slotIndex ->
            val completedDays = pastDates.count { date ->
                completedMap[date]?.contains(slotIndex) == true
            }
            completedDays.toFloat() / pastCount
        }

        // ── Total weekly % ───────────────────────────────────────────
        val totalSlots = pastDates.size * 7
        val totalCompleted = completedMap.entries
            .filter { it.key in pastDates }
            .sumOf { it.value.size }
        val totalWeekPct = if (totalSlots == 0) 0 else (totalCompleted * 100 / totalSlots)

        // ── Streak ───────────────────────────────────────────────────
        val todayCompletions = completedMap[todayStr]?.size ?: 0
        val todayIsActive = todayCompletions in 1..3
        var streakCount = 0
        val yesterdayOnward = dailyCompletions
            .filter { !it.isFuture && it.dateStr != todayStr }
            .sortedByDescending { it.dateStr }
        for (dc in yesterdayOnward) {
            if (dc.completedSlots >= 4) streakCount++ else break
        }

        // ── Best day ─────────────────────────────────────────────────
        val pastWithData = dailyCompletions.filter { !it.isFuture && it.completedSlots > 0 }
        val bestDay: BestDayState = if (pastWithData.size < 2) {
            BestDayState(dayName = null, pct = 0)
        } else {
            val best = pastWithData.maxByOrNull { it.pct } ?: pastWithData.last()
            BestDayState(dayName = best.dayName, pct = best.pct)
        }

        // ── Trend ────────────────────────────────────────────────────
        val daysForTrend = daysWithData.sortedByDescending { it.dateStr }
        val trend: TrendState = if (daysForTrend.size < 4) {
            TrendState.Insufficient
        } else {
            val recent2Avg = daysForTrend.take(2).map { it.pct }.average()
            val prev2Avg = daysForTrend.drop(2).take(2).map { it.pct }.average()
            val delta = (recent2Avg - prev2Avg).toInt()
            when {
                delta > 5 -> TrendState.Uptrend(delta)
                delta < -5 -> TrendState.Downtrend(-delta)
                else -> TrendState.Flat
            }
        }

        val dataHash = dailyCompletions.map { it.pct }.hashCode()

        return InsightsUiState.Ready(
            dailyCompletions = dailyCompletions,
            categoryCompletions = categoryCompletions,
            totalWeekPct = totalWeekPct,
            streak = StreakState(count = streakCount, todayIsActive = todayIsActive),
            bestDay = bestDay,
            trend = trend,
            dataHash = dataHash
        )
    }
}
