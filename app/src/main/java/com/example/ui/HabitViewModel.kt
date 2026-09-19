package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Habit
import com.example.data.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

data class DayStat(
    val day: Int,
    val value: Int,
    val status: String // "met", "miss", "partial", "future"
)

data class MonthStats(
    val days: List<DayStat>,
    val pct: Int,
    val dim: Int,
    val met: Int,
    val sum: Int
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = HabitRepository(db.habitDao())
    }

    val habits: StateFlow<List<Habit>> = repository.habits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val entriesMap: StateFlow<Map<String, Int>> = repository.entries
        .map { list ->
            list.associate { "${it.habitId}__${it.date}" to it.value }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _selectedMonth = MutableStateFlow(currentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    fun todayStr(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    fun currentMonthKey(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", y, m)
    }

    fun shiftSelectedMonth(delta: Int) {
        val cur = _selectedMonth.value
        val parts = cur.split("-")
        if (parts.size != 2) return
        val y = parts[0].toIntOrNull() ?: return
        val m = parts[1].toIntOrNull() ?: return
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, m - 1 + delta)
        val newY = cal.get(Calendar.YEAR)
        val newM = cal.get(Calendar.MONTH) + 1
        val nextKey = String.format(Locale.US, "%04d-%02d", newY, newM)
        if (delta > 0 && nextKey > currentMonthKey()) return
        _selectedMonth.value = nextKey
    }

    fun toggleTick(habitId: String) {
        viewModelScope.launch {
            val date = todayStr()
            val key = "${habitId}__$date"
            val isTicked = (entriesMap.value[key] ?: 0) > 0
            repository.toggleTick(habitId, date, isTicked)
        }
    }

    fun bumpNumeric(habitId: String, delta: Int) {
        viewModelScope.launch {
            val date = todayStr()
            val key = "${habitId}__$date"
            val currentVal = entriesMap.value[key] ?: 0
            val nextVal = (currentVal + delta).coerceAtLeast(0)
            repository.setEntry(habitId, date, nextVal)
        }
    }

    fun setNumericDirect(habitId: String, value: Int) {
        viewModelScope.launch {
            val date = todayStr()
            val nextVal = value.coerceAtLeast(0)
            repository.setEntry(habitId, date, nextVal)
        }
    }

    fun addHabit(name: String, type: String, target: Int?) {
        viewModelScope.launch {
            val currentList = habits.value
            val habit = Habit(
                id = UUID.randomUUID().toString().take(8),
                name = name.trim(),
                type = type,
                target = if (type == "numeric") target else null,
                orderIndex = currentList.size,
                createdAt = todayStr()
            )
            repository.addHabit(habit)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun moveHabit(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val list = habits.value.toMutableList()
            if (fromIndex !in list.indices || toIndex !in list.indices) return@launch
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            val updated = list.mapIndexed { index, habit ->
                habit.copy(orderIndex = index)
            }
            repository.updateHabits(updated)
        }
    }

    fun seedDefaultHabitsIfEmpty() {
        viewModelScope.launch {
            if (habits.value.isEmpty()) {
                val sample1 = Habit(
                    id = "sample_read",
                    name = "Daily reading",
                    type = "tick",
                    target = null,
                    orderIndex = 0,
                    createdAt = todayStr()
                )
                val sample2 = Habit(
                    id = "sample_water",
                    name = "Drink water (glasses)",
                    type = "numeric",
                    target = 8,
                    orderIndex = 1,
                    createdAt = todayStr()
                )
                repository.addHabit(sample1)
                repository.addHabit(sample2)
                repository.setEntry(sample1.id, todayStr(), 1)
                repository.setEntry(sample2.id, todayStr(), 4)
            }
        }
    }

    companion object {
        fun daysInMonth(year: Int, month: Int): Int {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        fun monthLabel(ym: String): String {
            val parts = ym.split("-")
            if (parts.size != 2) return ym
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 1
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
            return sdf.format(cal.time)
        }

        fun calculateMonthStats(habit: Habit, ym: String, entries: Map<String, Int>): MonthStats {
            val parts = ym.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val dim = daysInMonth(y, m)

            val todayCal = Calendar.getInstance()
            val currentYm = String.format(Locale.US, "%04d-%02d", todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH) + 1)
            val isCurrent = ym == currentYm
            val currentDay = todayCal.get(Calendar.DAY_OF_MONTH)
            val lastDue = if (isCurrent) currentDay else if (ym < currentYm) dim else 0

            val days = ArrayList<DayStat>(dim)
            var sum = 0
            var met = 0

            for (d in 1..dim) {
                val dateStr = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
                val key = "${habit.id}__$dateStr"
                val v = entries[key] ?: 0
                val due = d <= lastDue

                val status = if (habit.type == "tick") {
                    if (due) {
                        if (v > 0) {
                            met++
                            "met"
                        } else {
                            "miss"
                        }
                    } else {
                        "future"
                    }
                } else {
                    sum += v
                    val target = habit.target ?: 1
                    if (due) {
                        if (v >= target) {
                            met++
                            "met"
                        } else if (v > 0) {
                            "partial"
                        } else {
                            "miss"
                        }
                    } else {
                        "future"
                    }
                }
                days.add(DayStat(d, v, status))
            }

            val pct = if (habit.type == "tick") {
                if (dim > 0) ((met.toDouble() / dim) * 100).roundToInt() else 0
            } else {
                val target = habit.target ?: 1
                val maxTarget = target * dim
                if (maxTarget > 0) ((sum.toDouble() / maxTarget) * 100).roundToInt() else 0
            }

            return MonthStats(days, pct, dim, met, sum)
        }
    }
}
