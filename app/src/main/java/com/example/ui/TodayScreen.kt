package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.ui.theme.HabitBg
import com.example.ui.theme.HabitBorder
import com.example.ui.theme.HabitGold
import com.example.ui.theme.HabitGreen
import com.example.ui.theme.HabitGreenBg
import com.example.ui.theme.HabitInk
import com.example.ui.theme.HabitInkFaint
import com.example.ui.theme.HabitInkSoft
import com.example.ui.theme.HabitMoss
import com.example.ui.theme.HabitMossDark
import com.example.ui.theme.HabitNeutral
import com.example.ui.theme.HabitRed
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.HabitSurfaceAlt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TodayScreen(
    habits: List<Habit>,
    entriesMap: Map<String, Int>,
    todayDateStr: String,
    onToggleTick: (habitId: String) -> Unit,
    onBumpNumeric: (habitId: String, delta: Int) -> Unit,
    onSetNumeric: (habitId: String, value: Int) -> Unit,
    onDeleteHabit: (habitId: String) -> Unit,
    onMoveHabit: (fromIndex: Int, toIndex: Int) -> Unit,
    onOpenAdd: () -> Unit,
    onLoadSamples: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateLabel = remember {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.US)
        sdf.format(cal.time)
    }

    var habitToDelete by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HabitBg)
    ) {
        // Sticky Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HabitSurfaceAlt)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    color = HabitInk
                )
                Text(
                    text = dateLabel,
                    fontSize = 12.sp,
                    color = HabitInkSoft
                )
            }

            IconButton(
                onClick = onOpenAdd,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HabitMoss)
                    .testTag("add_habit_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add habit",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(HabitBorder)
        )

        if (habits.isEmpty()) {
            EmptyTodayView(
                onOpenAdd = onOpenAdd,
                onLoadSamples = onLoadSamples
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("habits_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = habits,
                    key = { _, habit -> habit.id }
                ) { index, habit ->
                    val monthStats = remember(habit, entriesMap) {
                        HabitViewModel.calculateMonthStats(
                            habit = habit,
                            ym = todayDateStr.take(7),
                            entries = entriesMap
                        )
                    }
                    val todayKey = "${habit.id}__$todayDateStr"
                    val todayVal = entriesMap[todayKey] ?: 0

                    HabitItemCard(
                        habit = habit,
                        todayVal = todayVal,
                        monthStats = monthStats,
                        isConfirmingDelete = habitToDelete == habit.id,
                        canMoveUp = index > 0,
                        canMoveDown = index < habits.size - 1,
                        onMoveUp = { onMoveHabit(index, index - 1) },
                        onMoveDown = { onMoveHabit(index, index + 1) },
                        onToggleTick = { onToggleTick(habit.id) },
                        onBump = { delta -> onBumpNumeric(habit.id, delta) },
                        onSetNumeric = { valNum -> onSetNumeric(habit.id, valNum) },
                        onRequestDelete = { habitToDelete = habit.id },
                        onConfirmDelete = {
                            onDeleteHabit(habit.id)
                            habitToDelete = null
                        },
                        onCancelDelete = { habitToDelete = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTodayView(
    onOpenAdd: () -> Unit,
    onLoadSamples: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Start your first habit",
            style = MaterialTheme.typography.titleLarge,
            color = HabitInk,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add something to track daily — a checkbox habit like \"read\", or a number habit like \"glasses of water\".",
            fontSize = 14.sp,
            color = HabitInkSoft,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = HabitMoss,
                contentColor = HabitSurface
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("empty_add_habit_button")
        ) {
            Text("Add a habit", fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onLoadSamples,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = HabitMossDark
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(HabitBorder)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("load_sample_habits_button")
        ) {
            Text("Load sample habits", fontSize = 13.sp)
        }
    }
}

@Composable
private fun HabitItemCard(
    habit: Habit,
    todayVal: Int,
    monthStats: MonthStats,
    isConfirmingDelete: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleTick: () -> Unit,
    onBump: (delta: Int) -> Unit,
    onSetNumeric: (value: Int) -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit
) {
    val leftBarColor = when {
        monthStats.pct >= 100 -> HabitGreen
        monthStats.pct >= 50 -> HabitGold
        else -> HabitRed
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HabitSurface)
            .border(0.5.dp, HabitBorder, RoundedCornerShape(14.dp))
            .testTag("habit_card_${habit.id}")
    ) {
        // Left colored status indicator
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .height(120.dp)
                .background(leftBarColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, end = 14.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reorder handles
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = if (canMoveUp) HabitInkFaint else HabitBorder,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = if (canMoveDown) HabitInkFaint else HabitBorder,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Name & Monthly summary
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (habit.type == "tick") {
                            "${monthStats.met}/${monthStats.dim} days this month"
                        } else {
                            "${monthStats.pct}% of monthly target"
                        },
                        fontSize = 12.sp,
                        color = HabitInkSoft
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Action area
                if (habit.type == "tick") {
                    val isDone = todayVal > 0
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDone) HabitGreenBg else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isDone) HabitGreen else HabitBorder,
                                shape = CircleShape
                            )
                            .clickable(onClick = onToggleTick)
                            .testTag("tick_habit_${habit.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = HabitGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RoundActionButton(
                            onClick = { onBump(-1) },
                            icon = Icons.Default.Remove,
                            contentDescription = "Decrease count",
                            modifier = Modifier.testTag("decrement_${habit.id}")
                        )

                        var showDirectInput by remember { mutableStateOf(false) }
                        var tempVal by remember(todayVal) { mutableStateOf(todayVal.toString()) }

                        if (showDirectInput) {
                            OutlinedTextField(
                                value = tempVal,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                                        tempVal = it
                                        val parsed = it.toIntOrNull() ?: 0
                                        onSetNumeric(parsed)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(54.dp)
                                    .height(42.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HabitMoss,
                                    unfocusedBorderColor = HabitBorder,
                                    focusedContainerColor = HabitSurfaceAlt,
                                    unfocusedContainerColor = HabitSurfaceAlt,
                                    focusedTextColor = HabitInk,
                                    unfocusedTextColor = HabitInk
                                )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HabitSurfaceAlt)
                                    .border(1.dp, HabitBorder, RoundedCornerShape(8.dp))
                                    .clickable { showDirectInput = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = todayVal.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HabitInk
                                )
                            }
                        }

                        RoundActionButton(
                            onClick = { onBump(1) },
                            icon = Icons.Default.Add,
                            contentDescription = "Increase count",
                            modifier = Modifier.testTag("increment_${habit.id}")
                        )
                    }
                }
            }

            // Numeric Target Progress Bar
            if (habit.type == "numeric" && habit.target != null && habit.target > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                val ratio = (todayVal.toFloat() / habit.target).coerceIn(0f, 1f)
                val isTargetMet = todayVal >= habit.target

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(HabitNeutral)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = ratio)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isTargetMet) HabitGreen else HabitGold)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$todayVal of ${habit.target} today",
                    fontSize = 11.sp,
                    color = HabitInkFaint
                )
            }

            // Bottom row: Delete and Confirmation
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = isConfirmingDelete) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delete this habit?",
                            fontSize = 11.sp,
                            color = HabitInkSoft
                        )
                        TextButton(
                            onClick = onConfirmDelete,
                            modifier = Modifier.testTag("confirm_delete_${habit.id}")
                        ) {
                            Text(
                                text = "Delete",
                                fontSize = 12.sp,
                                color = HabitRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = onCancelDelete,
                            modifier = Modifier.testTag("cancel_delete_${habit.id}")
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 12.sp,
                                color = HabitInkSoft
                            )
                        }
                    }
                }

                if (!isConfirmingDelete) {
                    IconButton(
                        onClick = onRequestDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete habit",
                            tint = HabitInkFaint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(HabitSurfaceAlt)
            .border(1.dp, HabitBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = HabitInk,
            modifier = Modifier.size(14.dp)
        )
    }
}
