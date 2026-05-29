package com.wefit.nourishcycle.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {

    /**
     * Single batch query for up to 7 dates in a cycle window.
     * Avoids combine() overflow (kotlinx.coroutines only has 5-arity combine()).
     * Grouping and dayIndex computation happen in Kotlin at read time.
     */
    @Query("SELECT * FROM meal_completions WHERE calendarDate IN (:dates)")
    fun getCompletionsForDates(dates: List<String>): Flow<List<CompletionEntity>>

    /**
     * Upsert: inserts if (calendarDate, mealSlotIndex) not present;
     * replaces the row if the UNIQUE constraint fires.
     * Requires Room 2.5+ — we use 2.7.1.
     */
    @Upsert
    suspend fun upsertCompletion(entity: CompletionEntity)

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
