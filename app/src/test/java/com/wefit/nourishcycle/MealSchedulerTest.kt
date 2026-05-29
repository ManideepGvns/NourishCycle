package com.wefit.nourishcycle

import com.wefit.nourishcycle.notification.MealNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MealNotificationScheduler data / configuration.
 * Pure logic — no Android context needed.
 */
class MealSchedulerTest {

    @Test
    fun `MEAL_TIMES contains exactly 7 entries`() {
        assertEquals(7, MealNotificationScheduler.MEAL_TIMES.size)
    }

    @Test
    fun `MEAL_TIMES slot indices are 0 through 6`() {
        val indices = MealNotificationScheduler.MEAL_TIMES.map { it.first }
        assertEquals((0..6).toList(), indices)
    }

    @Test
    fun `MEAL_TIMES times are in ascending order`() {
        val times = MealNotificationScheduler.MEAL_TIMES.map { (_, time) ->
            time.first * 60 + time.second  // convert to minutes since midnight
        }
        for (i in 0 until times.size - 1) {
            assertTrue(
                "Meal time at index $i (${times[i]} min) must be before index ${i + 1} (${times[i + 1]} min)",
                times[i] < times[i + 1]
            )
        }
    }

    @Test
    fun `MEAL_TIMES hours are within valid 24h range`() {
        MealNotificationScheduler.MEAL_TIMES.forEach { (slotIndex, time) ->
            val (hour, minute) = time
            assertTrue("slot $slotIndex hour $hour must be 0-23", hour in 0..23)
            assertTrue("slot $slotIndex minute $minute must be 0-59", minute in 0..59)
        }
    }

    @Test
    fun `MEAL_TIMES slot indices are unique`() {
        val indices = MealNotificationScheduler.MEAL_TIMES.map { it.first }
        assertEquals("Duplicate slot indices found", indices.size, indices.distinct().size)
    }

    @Test
    fun `first meal time is before noon`() {
        val firstHour = MealNotificationScheduler.MEAL_TIMES.first().second.first
        assertTrue("First meal should be before noon, got hour $firstHour", firstHour < 12)
    }

    @Test
    fun `last meal time is in the evening`() {
        val lastHour = MealNotificationScheduler.MEAL_TIMES.last().second.first
        assertTrue("Last meal should be after 18:00, got hour $lastHour", lastHour >= 18)
    }
}
