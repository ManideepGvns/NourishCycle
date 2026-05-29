package com.wefit.nourishcycle

import com.wefit.nourishcycle.notification.NotificationMessages
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for NotificationMessages.
 * Verifies that getContent returns valid, non-empty content for every slot × day combination.
 */
class NotificationMessagesTest {

    @Test
    fun `getContent returns non-null for every slot and day combination`() {
        for (slotIndex in 0..6) {
            for (dayIndex in 0..6) {
                val content = NotificationMessages.getContent(slotIndex, dayIndex)
                assertNotNull("content is null for slot=$slotIndex day=$dayIndex", content)
            }
        }
    }

    @Test
    fun `getContent returns non-blank title for every slot and day`() {
        for (slotIndex in 0..6) {
            for (dayIndex in 0..6) {
                val title = NotificationMessages.getContent(slotIndex, dayIndex).title
                assertFalse(
                    "title is blank for slot=$slotIndex day=$dayIndex",
                    title.isBlank()
                )
            }
        }
    }

    @Test
    fun `getContent returns non-blank body for every slot and day`() {
        for (slotIndex in 0..6) {
            for (dayIndex in 0..6) {
                val body = NotificationMessages.getContent(slotIndex, dayIndex).body
                assertFalse(
                    "body is blank for slot=$slotIndex day=$dayIndex",
                    body.isBlank()
                )
            }
        }
    }

    @Test
    fun `getContent does not throw for out-of-range slot index`() {
        // Should fall back to defaultContent, not throw
        val content = NotificationMessages.getContent(slotIndex = 99, dayIndex = 0)
        assertNotNull(content)
        assertFalse(content.title.isBlank())
    }

    @Test
    fun `getContent does not throw for out-of-range day index`() {
        val content = NotificationMessages.getContent(slotIndex = 0, dayIndex = 100)
        assertNotNull(content)
    }

    @Test
    fun `messages rotate — different dayIndex yields different content for slot 0`() {
        // At minimum, day 0 and day 1 for slot 0 should differ (pool has ≥7 entries)
        val day0 = NotificationMessages.getContent(slotIndex = 0, dayIndex = 0)
        val day1 = NotificationMessages.getContent(slotIndex = 0, dayIndex = 1)
        // Two consecutive days must not be the identical object — different rotation index
        assertFalse(
            "Expected day 0 and day 1 to have different titles, got: ${day0.title}",
            day0.title == day1.title
        )
    }
}
