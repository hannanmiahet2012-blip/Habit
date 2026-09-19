package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.HabitBorder
import com.example.ui.theme.HabitGreenBg
import com.example.ui.theme.HabitInkFaint
import com.example.ui.theme.HabitMoss
import com.example.ui.theme.HabitSurface

enum class HabitViewTab {
    TODAY,
    GROWTH
}

@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(HabitViewTab.TODAY) }
    var showAddSheet by remember { mutableStateOf(false) }

    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val entriesMap by viewModel.entriesMap.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val todayDateStr = remember { viewModel.todayStr() }
    val currentMonthStr = remember { viewModel.currentMonthKey() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            ColumnBottomNav(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                HabitViewTab.TODAY -> {
                    TodayScreen(
                        habits = habits,
                        entriesMap = entriesMap,
                        todayDateStr = todayDateStr,
                        onToggleTick = { viewModel.toggleTick(it) },
                        onBumpNumeric = { id, delta -> viewModel.bumpNumeric(id, delta) },
                        onSetNumeric = { id, value -> viewModel.setNumericDirect(id, value) },
                        onDeleteHabit = { viewModel.deleteHabit(it) },
                        onMoveHabit = { from, to -> viewModel.moveHabit(from, to) },
                        onOpenAdd = { showAddSheet = true },
                        onLoadSamples = { viewModel.seedDefaultHabitsIfEmpty() }
                    )
                }

                HabitViewTab.GROWTH -> {
                    GrowthScreen(
                        habits = habits,
                        entriesMap = entriesMap,
                        selectedMonth = selectedMonth,
                        currentMonth = currentMonthStr,
                        onShiftMonth = { delta -> viewModel.shiftSelectedMonth(delta) }
                    )
                }
            }
        }

        if (showAddSheet) {
            AddHabitSheet(
                onDismiss = { showAddSheet = false },
                onAddHabit = { name, type, target ->
                    viewModel.addHabit(name, type, target)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun ColumnBottomNav(
    currentTab: HabitViewTab,
    onTabSelected: (HabitViewTab) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HabitSurface)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(HabitBorder)
        )

        NavigationBar(
            containerColor = HabitSurface,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("bottom_navigation_bar")
        ) {
            NavigationBarItem(
                selected = currentTab == HabitViewTab.TODAY,
                onClick = { onTabSelected(HabitViewTab.TODAY) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Today",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = "Today",
                        fontSize = 11.sp,
                        fontWeight = if (currentTab == HabitViewTab.TODAY) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HabitMoss,
                    unselectedIconColor = HabitInkFaint,
                    selectedTextColor = HabitMoss,
                    unselectedTextColor = HabitInkFaint,
                    indicatorColor = HabitGreenBg
                ),
                modifier = Modifier.testTag("nav_tab_today")
            )

            NavigationBarItem(
                selected = currentTab == HabitViewTab.GROWTH,
                onClick = { onTabSelected(HabitViewTab.GROWTH) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Growth",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = "Growth",
                        fontSize = 11.sp,
                        fontWeight = if (currentTab == HabitViewTab.GROWTH) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HabitMoss,
                    unselectedIconColor = HabitInkFaint,
                    selectedTextColor = HabitMoss,
                    unselectedTextColor = HabitInkFaint,
                    indicatorColor = HabitGreenBg
                ),
                modifier = Modifier.testTag("nav_tab_growth")
            )
        }
    }
}
