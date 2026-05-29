package com.wefit.nourishcycle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {

    /**
     * Single batch query for up to 7 dates in a cycle window.
     */
    @Query("SELECT * FROM meal_completions WHERE calendarDate IN (:dates)")
    fun getCompletionsForDates(dates: List<String>): Flow<List<CompletionEntity>>

    /**
     * Fetch a single completion row by its composite business key.
     * Returns null when the slot has never been interacted with.
     */
    @Query("SELECT * FROM meal_completions WHERE calendarDate = :date AND mealSlotIndex = :slotIndex LIMIT 1")
    suspend fun getCompletionForSlot(date: String, slotIndex: Int): CompletionEntity?

    /** Update an existing row (preserves its auto-generated id). */
    @Update
    suspend fun updateCompletion(entity: CompletionEntity)

    /** Insert a brand-new completion row (first time a slot is tapped). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(entity: CompletionEntity)

    /**
     * Convenience query to load all entries for a single date
     * (used by InsightsViewModel to check day-level completion).
     */
    @Query("SELECT * FROM meal_completions WHERE calendarDate = :date")
    suspend fun getCompletionsForDate(date: String): List<CompletionEntity>

    /**
     * Delete all completions for a specific date (for "reset day" feature if added later).
     */
    @Query("DELETE FROM meal_completions WHERE calendarDate = :date")
    suspend fun deleteCompletionsForDate(date: String)
}
