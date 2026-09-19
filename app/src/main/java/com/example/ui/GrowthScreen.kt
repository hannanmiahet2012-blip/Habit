package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.HabitNeutral
import com.example.ui.theme.HabitRed
import com.example.ui.theme.HabitRedBg
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.HabitSurfaceAlt

@Composable
fun GrowthScreen(
    habits: List<Habit>,
    entriesMap: Map<String, Int>,
    selectedMonth: String,
    currentMonth: String,
    onShiftMonth: (delta: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthLabel = remember(selectedMonth) {
        HabitViewModel.monthLabel(selectedMonth)
    }
    val canGoNext = selectedMonth < currentMonth

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HabitBg)
    ) {
        // Sticky Header with Month Navigator
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
                    text = "Growth",
                    style = MaterialTheme.typography.titleLarge,
                    color = HabitInk
                )
                Text(
                    text = "Monthly progress by habit",
                    fontSize = 12.sp,
                    color = HabitInkSoft
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { onShiftMonth(-1) },
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HabitSurface)
                        .border(0.5.dp, HabitBorder, RoundedCornerShape(8.dp))
                        .testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = HabitInk,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = monthLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HabitInk,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onShiftMonth(1) },
                    enabled = canGoNext,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (canGoNext) HabitSurface else HabitSurfaceAlt)
                        .border(0.5.dp, HabitBorder, RoundedCornerShape(8.dp))
                        .testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = if (canGoNext) HabitInk else HabitInkFaint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(HabitBorder)
        )

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add a habit on the Today tab to see growth charts here.",
                    fontSize = 14.sp,
                    color = HabitInkSoft,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("growth_charts_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = habits,
                    key = { it.id }
                ) { habit ->
                    val stats = remember(habit, selectedMonth, entriesMap) {
                        HabitViewModel.calculateMonthStats(
                            habit = habit,
                            ym = selectedMonth,
                            entries = entriesMap
                        )
                    }

                    HabitGrowthChartCard(
                        habit = habit,
                        stats = stats
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitGrowthChartCard(
    habit: Habit,
    stats: MonthStats
) {
    val pctColor = when {
        stats.pct >= 100 -> HabitGreen
        stats.pct >= 50 -> HabitGold
        else -> HabitRed
    }
    val pctBg = when {
        stats.pct >= 100 -> HabitGreenBg
        stats.pct >= 50 -> Color(0xFFF7EFDD)
        else -> HabitRedBg
    }

    var selectedDayStat by remember { mutableStateOf<DayStat?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HabitSurface)
            .border(0.5.dp, HabitBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitInk
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (habit.type == "tick") {
                            "${stats.met}/${stats.dim} days ticked"
                        } else {
                            "${stats.sum} total · target ${habit.target ?: 1}/day"
                        },
                        fontSize = 12.sp,
                        color = HabitInkFaint
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(pctBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${stats.pct}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = pctColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tooltip if user tapped a bar
            AnimatedVisibility(visible = selectedDayStat != null) {
                selectedDayStat?.let { dayStat ->
                    val statusText = when (dayStat.status) {
                        "met" -> if (habit.type == "tick") "Done" else "${dayStat.value} (Target Met)"
                        "partial" -> "${dayStat.value} (Partial)"
                        "miss" -> if (habit.type == "tick") "Missed" else "0 (Missed)"
                        else -> "Upcoming"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(HabitSurfaceAlt)
                            .border(0.5.dp, HabitBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Day ${dayStat.day}: $statusText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = HabitInkSoft
                        )
                    }
                }
            }

            // Interactive Bar Chart Canvas
            val days = stats.days
            val targetVal = habit.target?.toFloat() ?: 1f
            val maxDayVal = remember(days, targetVal) {
                days.maxOfOrNull { it.value.toFloat() }?.coerceAtLeast(targetVal) ?: targetVal
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(days) {
                            detectTapGestures { offset ->
                                val totalWidth = size.width
                                val dayCount = days.size
                                if (dayCount > 0) {
                                    val colWidth = totalWidth / dayCount
                                    val tappedIndex = (offset.x / colWidth).toInt().coerceIn(0, dayCount - 1)
                                    selectedDayStat = if (selectedDayStat == days[tappedIndex]) null else days[tappedIndex]
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height - 20.dp.toPx() // leave room for day labels
                    val baselineY = canvasHeight
                    val dayCount = days.size
                    if (dayCount == 0) return@Canvas

                    val barGap = 2.dp.toPx()
                    val totalGap = barGap * (dayCount - 1)
                    val barWidth = ((canvasWidth - totalGap) / dayCount).coerceAtLeast(2.dp.toPx())

                    // Baseline guide
                    drawLine(
                        color = HabitBorder,
                        start = Offset(0f, baselineY),
                        end = Offset(canvasWidth, baselineY),
                        strokeWidth = 1.dp.toPx()
                    )

                    for (i in days.indices) {
                        val d = days[i]
                        val x = i * (barWidth + barGap)

                        val barHeight = if (habit.type == "tick") {
                            if (d.value > 0) canvasHeight * 0.85f else 4.dp.toPx()
                        } else {
                            if (d.value > 0) {
                                (canvasHeight * 0.85f * (d.value / maxDayVal)).coerceAtLeast(4.dp.toPx())
                            } else {
                                4.dp.toPx()
                            }
                        }

                        val barColor = when (d.status) {
                            "met" -> HabitGreen
                            "miss" -> HabitRed
                            "partial" -> HabitGold
                            else -> HabitNeutral
                        }

                        val y = baselineY - barHeight
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                // X-Axis Day Labels row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val interval = if (stats.dim > 28) 5 else 4
                    for (dayNum in 1..stats.dim step interval) {
                        Text(
                            text = dayNum.toString(),
                            fontSize = 9.sp,
                            color = HabitInkFaint
                        )
                    }
                    Text(
                        text = stats.dim.toString(),
                        fontSize = 9.sp,
                        color = HabitInkFaint
                    )
                }
            }
        }
    }
}
