package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {
    val habits: Flow<List<Habit>> = dao.getAllHabits()
    val entries: Flow<List<HabitEntry>> = dao.getAllEntries()

    suspend fun addHabit(habit: Habit) {
        dao.insertHabit(habit)
    }

    suspend fun updateHabits(habits: List<Habit>) {
        dao.updateHabits(habits)
    }

    suspend fun deleteHabit(id: String) {
        dao.deleteHabit(id)
        dao.deleteEntriesForHabit(id)
    }

    suspend fun setEntry(habitId: String, date: String, value: Int) {
        if (value <= 0) {
            dao.deleteEntry(habitId, date)
        } else {
            dao.insertEntry(HabitEntry(habitId, date, value))
        }
    }

    suspend fun toggleTick(habitId: String, date: String, isCurrentlyTicked: Boolean) {
        if (isCurrentlyTicked) {
            dao.deleteEntry(habitId, date)
        } else {
            dao.insertEntry(HabitEntry(habitId, date, 1))
        }
    }
}
