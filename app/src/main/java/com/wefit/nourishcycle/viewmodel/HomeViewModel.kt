package com.wefit.nourishcycle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wefit.nourishcycle.data.CompletionEntity
import com.wefit.nourishcycle.data.DietDatabase
import com.wefit.nourishcycle.data.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

enum class Greeting { MORNING, AFTERNOON, EVENING }

data class HomeUiState(
    val isLoading: Boolean = true,
    val cycleStartDate: LocalDate = PreferencesRepository.mostRecentMonday(),
    val cycleDates: List<String> = emptyList(),
    val completions: Map<String, Set<Int>> = emptyMap(),
    val todayPageIndex: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DietDatabase.getInstance(application)
    private val dao = db.dietDao()
    private val prefs = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Holds the active Room collection job so we can cancel it before resubscribing (fix L6)
    private var collectionJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Collect cycleStartDate first to prevent race with Room query (fix original plan)
            val startDate = prefs.cycleStartDate.first()
            startCollecting(startDate)
        }
    }

    /**
     * Starts (or restarts) the Room Flow collection for the given cycle start date.
     * Cancels any previously running collection job before starting — fixes duplicate
     * concurrent collectors that the original updateCycleStartDate created (L6).
     */
    private fun startCollecting(startDate: LocalDate) {
        val today = LocalDate.now()
        val dates = prefs.cycleWindowDates(startDate, today)
        val todayStr = today.format(PreferencesRepository.ISO_FORMAT)
        val todayIndex = dates.indexOfFirst { it == todayStr }.coerceAtLeast(0)

        _uiState.value = _uiState.value.copy(
            cycleStartDate = startDate,
            cycleDates = dates,
            todayPageIndex = todayIndex,
            isLoading = false
        )

        collectionJob?.cancel()
        collectionJob = viewModelScope.launch(Dispatchers.IO) {
            dao.getCompletionsForDates(dates).collect { entities ->
                val map = entities
                    .filter { it.isCompleted }
                    .groupBy { it.calendarDate }
                    .mapValues { (_, list) -> list.map { it.mealSlotIndex }.toSet() }
                _uiState.value = _uiState.value.copy(completions = map)
            }
        }
    }

    /**
     * Toggles a meal slot.
     *
     * Previous implementation used @Upsert with an auto-generated primary key.
     * @Upsert resolves conflicts by PRIMARY KEY only, so every call with id=0
     * was treated as a new INSERT — the UNIQUE index then fired a REPLACE that
     * deleted the old row and re-inserted, causing unreliable state.
     *
     * Fix: read the existing row directly from SQLite, then explicitly
     * @Update it (flipping isCompleted) or @Insert it for the first tap.
     * This preserves the row's id and makes the toggle 100% reliable.
     */
    fun toggleSlot(date: String, slotIndex: Int) {
        if (date.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getCompletionForSlot(date, slotIndex)
            if (existing != null) {
                dao.updateCompletion(existing.copy(isCompleted = !existing.isCompleted))
            } else {
                dao.insertCompletion(
                    CompletionEntity(calendarDate = date, mealSlotIndex = slotIndex, isCompleted = true)
                )
            }
        }
    }

    fun updateCycleStartDate(newDate: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setCycleStartDate(newDate)
            // startCollecting cancels the old collector before starting a new one (L6 fix)
            startCollecting(newDate)
        }
    }

    /** Computes the diet-plan day index (0–6) for a given date string. */
    fun dayPlanIndex(dateStr: String): Int {
        if (dateStr.isBlank()) return 0
        val cycleStart = _uiState.value.cycleStartDate
        val date = runCatching { LocalDate.parse(dateStr) }.getOrElse { LocalDate.now() }
        return prefs.dayIndex(date, cycleStart)
    }
}

fun computeGreeting(): Greeting {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> Greeting.MORNING
        hour < 17 -> Greeting.AFTERNOON
        else -> Greeting.EVENING
    }
}

fun Greeting.toDisplayString(): String = when (this) {
    Greeting.MORNING -> "Good Morning"
    Greeting.AFTERNOON -> "Good Afternoon"
    Greeting.EVENING -> "Good Evening"
}
