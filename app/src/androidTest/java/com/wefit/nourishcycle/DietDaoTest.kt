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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for DietDao using an in-memory Room database.
 *
 * Runs on device/emulator but uses in-memory DB so no real storage is touched.
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

    // ── upsertCompletion ──────────────────────────────────────────────────────

    @Test
    fun upsert_insertsNewRow() = runTest {
        val entity = CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 0, isCompleted = true)
        db.dietDao().upsertCompletion(entity)

        val results = db.dietDao().getCompletionsForDate("2026-05-29")
        assertEquals(1, results.size)
        assertTrue(results.first().isCompleted)
    }

    @Test
    fun upsert_updatesExistingRowOnDuplicateKey() = runTest {
        val dao = db.dietDao()

        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 1, isCompleted = true))
        // Upsert again with same (date, slot) but isCompleted = false — should update, not insert
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 1, isCompleted = false))

        val results = dao.getCompletionsForDate("2026-05-29")
        assertEquals("Upsert must update, not duplicate the row", 1, results.size)
        assertFalse("isCompleted must be updated to false", results.first().isCompleted)
    }

    @Test
    fun upsert_multipleSlots_areStoredIndependently() = runTest {
        val dao = db.dietDao()
        for (slot in 0..6) {
            dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = slot, isCompleted = true))
        }

        val results = dao.getCompletionsForDate("2026-05-29")
        assertEquals(7, results.size)
        assertTrue(results.all { it.isCompleted })
    }

    // ── getCompletionsForDates ────────────────────────────────────────────────

    @Test
    fun getCompletionsForDates_returnsOnlyRequestedDates() = runTest {
        val dao = db.dietDao()
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-25", mealSlotIndex = 0, isCompleted = true))
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-26", mealSlotIndex = 1, isCompleted = true))
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-27", mealSlotIndex = 2, isCompleted = true))

        // Query only two of the three dates
        val results = dao.getCompletionsForDates(listOf("2026-05-25", "2026-05-27")).first()
        assertEquals(2, results.size)
        assertTrue(results.any { it.calendarDate == "2026-05-25" })
        assertTrue(results.any { it.calendarDate == "2026-05-27" })
        assertFalse(results.any { it.calendarDate == "2026-05-26" })
    }

    @Test
    fun getCompletionsForDates_emptyQuery_returnsEmpty() = runTest {
        val dao = db.dietDao()
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-25", mealSlotIndex = 0, isCompleted = true))

        val results = dao.getCompletionsForDates(emptyList()).first()
        assertTrue("Expected empty result for empty date list", results.isEmpty())
    }

    @Test
    fun getCompletionsForDates_noMatchingDates_returnsEmpty() = runTest {
        val dao = db.dietDao()
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-25", mealSlotIndex = 0, isCompleted = true))

        val results = dao.getCompletionsForDates(listOf("2099-01-01")).first()
        assertTrue("Expected empty result when no dates match", results.isEmpty())
    }

    // ── Toggle simulation ─────────────────────────────────────────────────────

    @Test
    fun toggleSlot_completesAndUncompletes() = runTest {
        val dao = db.dietDao()
        val date = "2026-05-29"
        val slot = 3

        // First toggle — mark complete
        dao.upsertCompletion(CompletionEntity(calendarDate = date, mealSlotIndex = slot, isCompleted = true))
        var rows = dao.getCompletionsForDate(date)
        assertEquals(1, rows.size)
        assertTrue(rows.first().isCompleted)

        // Second toggle — mark incomplete
        dao.upsertCompletion(CompletionEntity(calendarDate = date, mealSlotIndex = slot, isCompleted = false))
        rows = dao.getCompletionsForDate(date)
        assertEquals("Must still be 1 row, not 2", 1, rows.size)
        assertFalse(rows.first().isCompleted)
    }

    // ── deleteCompletionsForDate ──────────────────────────────────────────────

    @Test
    fun deleteCompletionsForDate_removesAllSlotsForThatDate() = runTest {
        val dao = db.dietDao()
        for (slot in 0..3) {
            dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = slot, isCompleted = true))
        }
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-30", mealSlotIndex = 0, isCompleted = true))

        dao.deleteCompletionsForDate("2026-05-29")

        val deleted = dao.getCompletionsForDate("2026-05-29")
        val kept = dao.getCompletionsForDate("2026-05-30")

        assertTrue("All slots for deleted date should be gone", deleted.isEmpty())
        assertEquals("Other date's data must be untouched", 1, kept.size)
    }

    // ── Flow reactivity ───────────────────────────────────────────────────────

    @Test
    fun getCompletionsForDates_flowEmitsUpdateWhenDataChanges() = runTest {
        val dao = db.dietDao()
        val dates = listOf("2026-05-29")

        // Collect initial (empty) value
        val initial = dao.getCompletionsForDates(dates).first()
        assertTrue(initial.isEmpty())

        // Insert a row
        dao.upsertCompletion(CompletionEntity(calendarDate = "2026-05-29", mealSlotIndex = 0, isCompleted = true))

        // Collect again — must reflect new row
        val updated = dao.getCompletionsForDates(dates).first()
        assertEquals(1, updated.size)
    }
}
