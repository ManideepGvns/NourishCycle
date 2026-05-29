package com.wefit.nourishcycle

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wefit.nourishcycle.data.CompletionEntity
import com.wefit.nourishcycle.data.DietDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for DietDao using an in-memory Room database.
 * Covers: insert, update, toggle, batch query, flow reactivity, delete.
 */
@RunWith(AndroidJUnit4::class)
class DietDaoTest {

    private lateinit var db: DietDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = db.close()

    // ── insertCompletion ──────────────────────────────────────────────────────

    @Test
    fun insert_newSlot_storesRow() = runTest {
        val dao = db.dietDao()
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 0, isCompleted = true))

        val row = dao.getCompletionForSlot("2026-05-29", 0)
        assertNotNull(row)
        assertTrue(row!!.isCompleted)
    }

    @Test
    fun insert_duplicateSlot_isIgnored() = runTest {
        val dao = db.dietDao()
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 0, isCompleted = true))
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 0, isCompleted = false))

        val rows = dao.getCompletionsForDate("2026-05-29")
        assertEquals("Second insert ignored — still only 1 row", 1, rows.size)
        assertTrue("Original value preserved", rows.first().isCompleted)
    }

    // ── getCompletionForSlot ──────────────────────────────────────────────────

    @Test
    fun getCompletionForSlot_returnsNullWhenMissing() = runTest {
        val row = db.dietDao().getCompletionForSlot("2026-05-29", 0)
        assertNull(row)
    }

    @Test
    fun getCompletionForSlot_returnsCorrectRow() = runTest {
        val dao = db.dietDao()
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 3, isCompleted = true))
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 5, isCompleted = false))

        val row = dao.getCompletionForSlot("2026-05-29", 3)
        assertNotNull(row)
        assertEquals(3, row!!.mealSlotIndex)
        assertTrue(row.isCompleted)
    }

    // ── updateCompletion (toggle) ─────────────────────────────────────────────

    @Test
    fun update_togglesIsCompleted() = runTest {
        val dao = db.dietDao()
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 2, isCompleted = true))

        val existing = dao.getCompletionForSlot("2026-05-29", 2)!!
        dao.updateCompletion(existing.copy(isCompleted = false))

        val updated = dao.getCompletionForSlot("2026-05-29", 2)
        assertNotNull(updated)
        assertFalse("isCompleted must be flipped to false", updated!!.isCompleted)
        assertEquals("Row count must still be 1", 1, dao.getCompletionsForDate("2026-05-29").size)
    }

    /** Simulates the exact HomeViewModel.toggleSlot logic: check → uncheck → check */
    @Test
    fun toggleSlot_checkUncheckCheck_worksCorrectly() = runTest {
        val dao = db.dietDao()
        val date = "2026-05-30"
        val slot = 4

        // First toggle: slot is new → insert as checked
        val before1 = dao.getCompletionForSlot(date, slot)
        assertNull("No row yet", before1)
        dao.insertCompletion(CompletionEntity(calendarDate = date, mealSlotIndex = slot, isCompleted = true))
        assertTrue("After first toggle: checked", dao.getCompletionForSlot(date, slot)!!.isCompleted)

        // Second toggle: row exists → flip to unchecked
        val before2 = dao.getCompletionForSlot(date, slot)!!
        dao.updateCompletion(before2.copy(isCompleted = !before2.isCompleted))
        assertFalse("After second toggle: unchecked", dao.getCompletionForSlot(date, slot)!!.isCompleted)
        assertEquals("Still only 1 row in DB", 1, dao.getCompletionsForDate(date).size)

        // Third toggle: row exists → flip back to checked
        val before3 = dao.getCompletionForSlot(date, slot)!!
        dao.updateCompletion(before3.copy(isCompleted = !before3.isCompleted))
        assertTrue("After third toggle: checked again", dao.getCompletionForSlot(date, slot)!!.isCompleted)
        assertEquals("Still only 1 row in DB", 1, dao.getCompletionsForDate(date).size)
    }

    // ── getCompletionsForDates ────────────────────────────────────────────────

    @Test
    fun getCompletionsForDates_returnsOnlyRequestedDates() = runTest {
        val dao = db.dietDao()
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-25", mealSlotIndex = 0, isCompleted = true))
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-26", mealSlotIndex = 1, isCompleted = true))
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-27", mealSlotIndex = 2, isCompleted = true))

        val results = dao.getCompletionsForDates(listOf("2026-05-25", "2026-05-27")).first()
        assertEquals(2, results.size)
        assertTrue(results.any { it.calendarDate == "2026-05-25" })
        assertFalse(results.any { it.calendarDate == "2026-05-26" })
    }

    @Test
    fun getCompletionsForDates_emptyList_returnsEmpty() = runTest {
        db.dietDao().insertCompletion(CompletionEntity(calendarDate = "2026-05-25", mealSlotIndex = 0, isCompleted = true))
        val results = db.dietDao().getCompletionsForDates(emptyList()).first()
        assertTrue(results.isEmpty())
    }

    // ── deleteCompletionsForDate ──────────────────────────────────────────────

    @Test
    fun deleteCompletionsForDate_removesAllSlotsForThatDate() = runTest {
        val dao = db.dietDao()
        for (slot in 0..3) {
            dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = slot, isCompleted = true))
        }
        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-30", mealSlotIndex = 0, isCompleted = true))

        dao.deleteCompletionsForDate("2026-05-29")

        assertTrue(dao.getCompletionsForDate("2026-05-29").isEmpty())
        assertEquals(1, dao.getCompletionsForDate("2026-05-30").size)
    }

    // ── Flow reactivity ───────────────────────────────────────────────────────

    @Test
    fun flow_emitsUpdateAfterInsert() = runTest {
        val dao = db.dietDao()
        assertTrue(dao.getCompletionsForDates(listOf("2026-05-29")).first().isEmpty())

        dao.insertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 0, isCompleted = true))

        val updated = dao.getCompletionsForDates(listOf("2026-05-29")).first()
        assertEquals(1, updated.size)
    }
}
