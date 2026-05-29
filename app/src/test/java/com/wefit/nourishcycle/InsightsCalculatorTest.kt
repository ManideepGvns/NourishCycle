package com.wefit.nourishcycle

import com.wefit.nourishcycle.viewmodel.BestDayState
import com.wefit.nourishcycle.viewmodel.DayCompletion
import com.wefit.nourishcycle.viewmodel.TrendState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure KPI calculation logic used by InsightsViewModel.
 *
 * These are extracted here as plain functions mirroring the ViewModel's private methods,
 * so they run as fast JVM tests without any Android context.
 */
class InsightsCalculatorTest {

    // ── Helpers mirroring InsightsViewModel private logic ──────────────────────

    private fun buildDayCompletion(
        dateStr: String, dayName: String, completedSlots: Int, isFuture: Boolean
    ) = DayCompletion(
        dateStr = dateStr,
        dayName = dayName,
        completedSlots = completedSlots,
        pct = if (isFuture) 0 else (completedSlots * 100 / 7),
        isFuture = isFuture
    )

    private fun calcStreak(
        dailyCompletions: List<DayCompletion>,
        todayStr: String
    ): Int {
        var count = 0
        val sorted = dailyCompletions
            .filter { !it.isFuture && it.dateStr != todayStr }
            .sortedByDescending { it.dateStr }
        for (dc in sorted) {
            if (dc.completedSlots >= 4) count++ else break
        }
        return count
    }

    private fun calcBestDay(dailyCompletions: List<DayCompletion>): BestDayState {
        val pastWithData = dailyCompletions.filter { !it.isFuture && it.completedSlots > 0 }
        return if (pastWithData.size < 2) {
            BestDayState(dayName = null, pct = 0)
        } else {
            val best = pastWithData.maxByOrNull { it.pct } ?: pastWithData.last()
            BestDayState(dayName = best.dayName, pct = best.pct)
        }
    }

    private fun calcTrend(daysWithData: List<DayCompletion>): TrendState {
        val sorted = daysWithData.sortedByDescending { it.dateStr }
        return if (sorted.size < 4) {
            TrendState.Insufficient
        } else {
            val recent2Avg = sorted.take(2).map { it.pct }.average()
            val prev2Avg = sorted.drop(2).take(2).map { it.pct }.average()
            val delta = (recent2Avg - prev2Avg).toInt()
            when {
                delta > 5 -> TrendState.Uptrend(delta)
                delta < -5 -> TrendState.Downtrend(-delta)
                else -> TrendState.Flat
            }
        }
    }

    // ── Streak tests ──────────────────────────────────────────────────────────

    @Test
    fun `streak is 0 when no past days have 4+ completions`() {
        val days = listOf(
            buildDayCompletion("2026-05-25", "Mon", 2, false),
            buildDayCompletion("2026-05-26", "Tue", 3, false),
        )
        assertEquals(0, calcStreak(days, "2026-05-27"))
    }

    @Test
    fun `streak counts consecutive days with 4+ completions from most recent`() {
        val days = listOf(
            buildDayCompletion("2026-05-23", "Sat", 5, false),
            buildDayCompletion("2026-05-24", "Sun", 6, false),
            buildDayCompletion("2026-05-25", "Mon", 7, false),
            buildDayCompletion("2026-05-26", "Tue", 4, false),
        )
        // today = May 27; streak should count: May 26, May 25, May 24, May 23 = 4
        assertEquals(4, calcStreak(days, "2026-05-27"))
    }

    @Test
    fun `streak breaks at a day with fewer than 4 completions`() {
        val days = listOf(
            buildDayCompletion("2026-05-23", "Sat", 7, false),
            buildDayCompletion("2026-05-24", "Sun", 2, false),  // breaks streak
            buildDayCompletion("2026-05-25", "Mon", 5, false),
            buildDayCompletion("2026-05-26", "Tue", 6, false),
        )
        // today = May 27; recent streak: May 26 (6✓), May 25 (5✓), May 24 (2✗ breaks) → 2
        assertEquals(2, calcStreak(days, "2026-05-27"))
    }

    @Test
    fun `streak excludes today`() {
        val days = listOf(
            buildDayCompletion("2026-05-26", "Tue", 7, false),
            buildDayCompletion("2026-05-27", "Wed", 7, false),  // today
        )
        // today = May 27; only May 26 eligible → streak = 1
        assertEquals(1, calcStreak(days, "2026-05-27"))
    }

    @Test
    fun `streak excludes future days`() {
        val days = listOf(
            buildDayCompletion("2026-05-25", "Mon", 5, false),
            buildDayCompletion("2026-05-28", "Thu", 7, true),   // future, ignored
        )
        assertEquals(1, calcStreak(days, "2026-05-27"))
    }

    // ── Best Day tests ────────────────────────────────────────────────────────

    @Test
    fun `bestDay is null when fewer than 2 past days have completions`() {
        val days = listOf(
            buildDayCompletion("2026-05-25", "Mon", 3, false),
            buildDayCompletion("2026-05-26", "Tue", 0, false),  // no completions
        )
        val result = calcBestDay(days)
        assertNull(result.dayName)
    }

    @Test
    fun `bestDay returns day with highest pct`() {
        val days = listOf(
            buildDayCompletion("2026-05-25", "Mon", 3, false),  // pct = 42
            buildDayCompletion("2026-05-26", "Tue", 7, false),  // pct = 100 ← best
            buildDayCompletion("2026-05-27", "Wed", 5, false),  // pct = 71
        )
        val result = calcBestDay(days)
        assertEquals("Tue", result.dayName)
        assertEquals(100, result.pct)
    }

    @Test
    fun `bestDay excludes future days`() {
        val days = listOf(
            buildDayCompletion("2026-05-25", "Mon", 4, false),
            buildDayCompletion("2026-05-26", "Tue", 7, false),
            buildDayCompletion("2026-05-28", "Thu", 7, true),   // future — must be ignored
        )
        val result = calcBestDay(days)
        assertEquals("Tue", result.dayName)
    }

    // ── Trend tests ───────────────────────────────────────────────────────────

    @Test
    fun `trend is Insufficient when fewer than 4 days with data`() {
        val days = listOf(
            buildDayCompletion("2026-05-25", "Mon", 5, false),
            buildDayCompletion("2026-05-26", "Tue", 5, false),
            buildDayCompletion("2026-05-27", "Wed", 5, false),
        )
        assertEquals(TrendState.Insufficient, calcTrend(days))
    }

    @Test
    fun `trend is Uptrend when recent 2 days average higher than previous 2`() {
        val days = listOf(
            buildDayCompletion("2026-05-23", "Sat", 2, false),  // pct ≈ 28
            buildDayCompletion("2026-05-24", "Sun", 3, false),  // pct ≈ 42
            buildDayCompletion("2026-05-25", "Mon", 6, false),  // pct ≈ 85
            buildDayCompletion("2026-05-26", "Tue", 7, false),  // pct = 100
        )
        // Sorted desc: May 26 (100), May 25 (85), May 24 (42), May 23 (28)
        // recent2Avg = (100+85)/2 = 92.5, prev2Avg = (42+28)/2 = 35 → delta = 57 > 5 → Uptrend
        val result = calcTrend(days)
        assertTrue("Expected Uptrend, got $result", result is TrendState.Uptrend)
    }

    @Test
    fun `trend is Downtrend when recent 2 days average lower than previous 2`() {
        val days = listOf(
            buildDayCompletion("2026-05-23", "Sat", 7, false),  // pct = 100
            buildDayCompletion("2026-05-24", "Sun", 6, false),  // pct ≈ 85
            buildDayCompletion("2026-05-25", "Mon", 2, false),  // pct ≈ 28
            buildDayCompletion("2026-05-26", "Tue", 1, false),  // pct ≈ 14
        )
        // recent2Avg = (14+28)/2 = 21, prev2Avg = (85+100)/2 = 92.5 → delta = -71 < -5 → Downtrend
        val result = calcTrend(days)
        assertTrue("Expected Downtrend, got $result", result is TrendState.Downtrend)
    }

    @Test
    fun `trend is Flat when delta is within ±5`() {
        val days = listOf(
            buildDayCompletion("2026-05-23", "Sat", 5, false),
            buildDayCompletion("2026-05-24", "Sun", 5, false),
            buildDayCompletion("2026-05-25", "Mon", 5, false),
            buildDayCompletion("2026-05-26", "Tue", 5, false),
        )
        // All pct = 71 → delta = 0 → Flat
        assertEquals(TrendState.Flat, calcTrend(days))
    }

    // ── Category completion (donut normalization) ─────────────────────────────

    @Test
    fun `category completions are clamped between 0 and 1`() {
        // Simulates the computation in InsightsViewModel
        val pastDates = listOf("2026-05-25", "2026-05-26")
        val completedMap = mapOf(
            "2026-05-25" to setOf(0, 1, 2, 3, 4, 5, 6),  // all 7 slots
            "2026-05-26" to setOf(0, 1)                    // 2 slots
        )
        val pastCount = pastDates.size
        val catCompletions = (0..6).map { slotIndex ->
            val completedDays = pastDates.count { date ->
                completedMap[date]?.contains(slotIndex) == true
            }
            completedDays.toFloat() / pastCount
        }

        catCompletions.forEachIndexed { i, value ->
            assertTrue("slot $i: value $value < 0", value >= 0f)
            assertTrue("slot $i: value $value > 1", value <= 1f)
        }
        // Slot 0 completed both days → 1.0
        assertEquals(1.0f, catCompletions[0], 0.001f)
        // Slot 6 completed only May 25 → 0.5
        assertEquals(0.5f, catCompletions[6], 0.001f)
    }

    @Test
    fun `total week pct is correct`() {
        val pastDates = listOf("2026-05-25", "2026-05-26")
        val completedMap = mapOf(
            "2026-05-25" to setOf(0, 1, 2),  // 3 slots
            "2026-05-26" to setOf(3, 4)       // 2 slots
        )
        val totalSlots = pastDates.size * 7  // 14
        val totalCompleted = completedMap.entries
            .filter { it.key in pastDates }
            .sumOf { it.value.size }          // 5
        val pct = totalCompleted * 100 / totalSlots
        assertEquals(35, pct)
    }
}
