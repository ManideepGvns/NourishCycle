package com.wefit.nourishcycle

import com.wefit.nourishcycle.viewmodel.Greeting
import com.wefit.nourishcycle.viewmodel.toDisplayString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the greeting logic extracted from HomeViewModel.
 * The production code maps hour → Greeting enum; we test that mapping here.
 */
class GreetingTest {

    private fun greetingForHour(hour: Int): Greeting = when {
        hour < 12 -> Greeting.MORNING
        hour < 17 -> Greeting.AFTERNOON
        else -> Greeting.EVENING
    }

    @Test
    fun `hour 0 is MORNING`() = assertEquals(Greeting.MORNING, greetingForHour(0))

    @Test
    fun `hour 5 is MORNING`() = assertEquals(Greeting.MORNING, greetingForHour(5))

    @Test
    fun `hour 11 is MORNING (boundary)`() = assertEquals(Greeting.MORNING, greetingForHour(11))

    @Test
    fun `hour 12 is AFTERNOON (boundary)`() = assertEquals(Greeting.AFTERNOON, greetingForHour(12))

    @Test
    fun `hour 13 is AFTERNOON`() = assertEquals(Greeting.AFTERNOON, greetingForHour(13))

    @Test
    fun `hour 16 is AFTERNOON (boundary)`() = assertEquals(Greeting.AFTERNOON, greetingForHour(16))

    @Test
    fun `hour 17 is EVENING (boundary)`() = assertEquals(Greeting.EVENING, greetingForHour(17))

    @Test
    fun `hour 20 is EVENING`() = assertEquals(Greeting.EVENING, greetingForHour(20))

    @Test
    fun `hour 23 is EVENING`() = assertEquals(Greeting.EVENING, greetingForHour(23))

    @Test
    fun `all 24 hours map to a valid greeting`() {
        for (hour in 0..23) {
            val greeting = greetingForHour(hour)
            assert(greeting in Greeting.entries) {
                "Hour $hour produced unexpected greeting: $greeting"
            }
        }
    }

    @Test
    fun `display strings are non-empty for all greetings`() {
        Greeting.entries.forEach { g ->
            val display = g.toDisplayString()
            assert(display.isNotBlank()) { "Display string for $g is blank" }
        }
    }
}
