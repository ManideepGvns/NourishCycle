package com.wefit.nourishcycle.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nourish_prefs")

class PreferencesRepository(private val context: Context) {

    companion object {
        private val CYCLE_START_DATE_KEY = stringPreferencesKey("cycle_start_date")
        val ISO_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        /** Returns the most recent Monday on or before today. */
        fun mostRecentMonday(): LocalDate {
            val today = LocalDate.now()
            val daysSinceMonday = today.dayOfWeek.value - DayOfWeek.MONDAY.value
            return today.minusDays(daysSinceMonday.toLong().coerceAtLeast(0))
        }
    }

    /** Emits the stored cycle start date, defaulting to most recent Monday. */
    val cycleStartDate: Flow<LocalDate> = context.dataStore.data.map { prefs ->
        val raw = prefs[CYCLE_START_DATE_KEY]
        if (raw != null) {
            runCatching { LocalDate.parse(raw, ISO_FORMAT) }.getOrElse { mostRecentMonday() }
        } else {
            mostRecentMonday()
        }
    }

    suspend fun setCycleStartDate(date: LocalDate) {
        context.dataStore.edit { prefs ->
            prefs[CYCLE_START_DATE_KEY] = date.format(ISO_FORMAT)
        }
    }

    /**
     * Computes the 7-day cycle window that contains today, aligned to [cycleStart].
     *
     * Fix for L1: the window is NOT always [cycleStart .. cycleStart+6].
     * It advances by 7-day increments to ensure today is always inside the window.
     *
     * Example: cycleStart = May 25, today = June 2
     *   daysSinceStart = 8, cyclesElapsed = 1, windowStart = June 1
     *   → window = [Jun 1 .. Jun 7], today (Jun 2) is at index 1. ✓
     */
    fun cycleWindowDates(cycleStart: LocalDate, today: LocalDate = LocalDate.now()): List<String> {
        val daysSinceStart = ChronoUnit.DAYS.between(cycleStart, today)
        val cyclesElapsed = if (daysSinceStart >= 0) daysSinceStart / 7 else 0
        val windowStart = cycleStart.plusDays(cyclesElapsed * 7)
        return (0..6).map { windowStart.plusDays(it.toLong()).format(ISO_FORMAT) }
    }

    /**
     * Computes the day index (0–6) for a given [date] relative to [cycleStart].
     * Uses ((days % 7) + 7) % 7 to handle dates before the cycle start safely.
     */
    fun dayIndex(date: LocalDate, cycleStart: LocalDate): Int {
        val days = date.toEpochDay() - cycleStart.toEpochDay()
        return ((days % 7).toInt() + 7) % 7
    }
}
