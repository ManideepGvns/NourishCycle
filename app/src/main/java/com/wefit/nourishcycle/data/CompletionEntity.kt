package com.wefit.nourishcycle.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks whether a meal slot was completed on a given calendar date.
 *
 * Key design decisions:
 *  - Keyed by calendarDate (ISO string) — never auto-resets.
 *  - No dayIndex column — computed at read time from cycleStartDate to
 *    avoid stale values if the user changes their cycle start date.
 *  - UNIQUE constraint on (calendarDate, mealSlotIndex) ensures upsert
 *    is safe: toggling the same slot twice doesn't create duplicate rows.
 */
@Entity(
    tableName = "meal_completions",
    indices = [Index(value = ["calendarDate", "mealSlotIndex"], unique = true)]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val calendarDate: String,   // ISO format: "2026-05-29"
    val mealSlotIndex: Int,     // 0–6 matching MealCategory.slotIndex
    val isCompleted: Boolean
)
