package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "tick" or "numeric"
    val target: Int?,
    val orderIndex: Int = 0,
    val createdAt: String
)

@Entity(tableName = "habit_entries", primaryKeys = ["habitId", "date"])
data class HabitEntry(
    val habitId: String,
    val date: String, // "YYYY-MM-DD"
    val value: Int
)
