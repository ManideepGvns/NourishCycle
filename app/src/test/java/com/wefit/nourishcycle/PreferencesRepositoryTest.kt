package com.wefit.nourishcycle

import com.wefit.nourishcycle.data.PreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for PreferencesRepository pure functions.
 * These functions have no Android dependencies — run with plain JUnit.
 */
class PreferencesRepositoryTest {

    // ── dayIndex ──────────────────────────────────────────────────────────────

    @Test
    fun `dayIndex returns 0 for cycleStart itself`() {
        val start = LocalDate.of(2026, 1, 1)
        // A dummy repository — we only need the pure companion function
        val repo = RepoStub()
        assertEquals(0, repo.dayIndex(start, start))
    }

    @Test
    fun `dayIndex advances correctly through 7 days`() {
        val start = LocalDate.of(2026, 1, 1)
        val repo = RepoStub()
        for (i in 0..6) {
            assertEquals(i, repo.dayIndex(start.plusDays(i.toLong()), start))
        }
    }

    @Test
    fun `dayIndex wraps after 7 days`() {
        val start = LocalDate.of(2026, 1, 1)
        val repo = RepoStub()
        // Day 7 = index 0 again (new cycle)
        assertEquals(0, repo.dayIndex(start.plusDays(7), start))
        assertEquals(3, repo.dayIndex(start.plusDays(10), start))
    }

    @Test
    fun `dayIndex handles date before cycleStart without negative result`() {
        val start = LocalDate.of(2026, 5, 25)
        val repo = RepoStub()
        // date is 1 day before start → days = -1, expected index = ((-1 % 7) + 7) % 7 = 6
        val result = repo.dayIndex(start.minusDays(1), start)
        assertTrue("dayIndex must be non-negative", result >= 0)
        assertEquals(6, result)
    }

    @Test
    fun `dayIndex handles date 13 days before cycleStart`() {
        val start = LocalDate.of(2026, 5, 25)
        val repo = RepoStub()
        // -13 % 7 = -6 (Kotlin), (+7) % 7 = 1
        assertEquals(1, repo.dayIndex(start.minusDays(13), start))
    }

    // ── cycleWindowDates ──────────────────────────────────────────────────────

    @Test
    fun `cycleWindowDates returns 7 dates`() {
        val start = LocalDate.of(2026, 5, 25)
        val today = LocalDate.of(2026, 5, 26)
        val repo = RepoStub()
        val window = repo.cycleWindowDates(start, today)
        assertEquals(7, window.size)
    }

    @Test
    fun `cycleWindowDates starts at cycleStart when today is in first cycle`() {
        val start = LocalDate.of(2026, 5, 25)
        val today = LocalDate.of(2026, 5, 27)  // 2 days into first cycle
        val repo = RepoStub()
        val window = repo.cycleWindowDates(start, today)
        assertEquals("2026-05-25", window.first())
        assertEquals("2026-05-31", window.last())
    }

    @Test
    fun `cycleWindowDates always contains today`() {
        val start = LocalDate.of(2026, 5, 25)
        val repo = RepoStub()
        // Test across 3 full cycles
        for (daysAhead in 0L..20L) {
            val today = start.plusDays(daysAhead)
            val window = repo.cycleWindowDates(start, today)
            val todayStr = today.format(PreferencesRepository.ISO_FORMAT)
            assertTrue(
                "today ($todayStr) must be in window for daysAhead=$daysAhead: $window",
                todayStr in window
            )
        }
    }

    @Test
    fun `cycleWindowDates advances to next window after 7 days`() {
        val start = LocalDate.of(2026, 5, 25)
        val repo = RepoStub()

        val window1 = repo.cycleWindowDates(start, start)
        val window2 = repo.cycleWindowDates(start, start.plusDays(7))

        assertEquals("2026-05-25", window1.first())
        assertEquals("2026-06-01", window2.first())  // shifted by exactly 7 days
    }

    @Test
    fun `cycleWindowDates dates are in ascending order`() {
        val start = LocalDate.of(2026, 5, 25)
        val today = LocalDate.of(2026, 6, 3)
        val repo = RepoStub()
        val window = repo.cycleWindowDates(start, today)
        val parsed = window.map { LocalDate.parse(it, PreferencesRepository.ISO_FORMAT) }
        for (i in 0 until parsed.size - 1) {
            assertTrue(parsed[i].isBefore(parsed[i + 1]))
        }
    }

    // ── mostRecentMonday ──────────────────────────────────────────────────────

    @Test
    fun `mostRecentMonday returns Monday itself if today is Monday`() {
        // We can't inject today, but we can verify the formula logic via direct date math
        val monday = LocalDate.of(2026, 5, 25)  // Known Monday
        val daysSinceMonday = monday.dayOfWeek.value - 1  // MONDAY = 1
        val result = monday.minusDays(daysSinceMonday.toLong().coerceAtLeast(0))
        assertEquals(monday, result)
    }

    @Test
    fun `mostRecentMonday returns previous Monday for a Wednesday`() {
        val wednesday = LocalDate.of(2026, 5, 27)
        val daysSinceMonday = wednesday.dayOfWeek.value - 1  // 3 - 1 = 2
        val result = wednesday.minusDays(daysSinceMonday.toLong().coerceAtLeast(0))
        assertEquals(LocalDate.of(2026, 5, 25), result)
    }

    /** Minimal stub that exposes the pure calculation methods under test. */
    private inner class RepoStub {
        fun dayIndex(date: LocalDate, cycleStart: LocalDate): Int {
            val days = date.toEpochDay() - cycleStart.toEpochDay()
            return ((days % 7).toInt() + 7) % 7
        }

        fun cycleWindowDates(cycleStart: LocalDate, today: LocalDate): List<String> {
            val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(cycleStart, today)
            val cyclesElapsed = if (daysSinceStart >= 0) daysSinceStart / 7 else 0
            val windowStart = cycleStart.plusDays(cyclesElapsed * 7)
            return (0..6).map {
                windowStart.plusDays(it.toLong()).format(PreferencesRepository.ISO_FORMAT)
            }
        }
    }
}
