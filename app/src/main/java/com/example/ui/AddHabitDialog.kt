package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HabitBorder
import com.example.ui.theme.HabitGreenBg
import com.example.ui.theme.HabitInk
import com.example.ui.theme.HabitInkFaint
import com.example.ui.theme.HabitInkSoft
import com.example.ui.theme.HabitMoss
import com.example.ui.theme.HabitMossDark
import com.example.ui.theme.HabitRed
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.HabitSurfaceAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    onAddHabit: (name: String, type: String, target: Int?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("tick") }
    var targetStr by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HabitSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = HabitInk
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_habit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = HabitInkSoft
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Name
            Text(
                text = "Habit name",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = HabitInkSoft
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("Read, drink water, stretch…", color = HabitInkFaint) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_name_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HabitMoss,
                    unfocusedBorderColor = HabitBorder,
                    focusedContainerColor = HabitSurfaceAlt,
                    unfocusedContainerColor = HabitSurfaceAlt,
                    focusedTextColor = HabitInk,
                    unfocusedTextColor = HabitInk
                ),
                keyboardOptions = KeyboardOptions(imeAction = if (type == "tick") ImeAction.Done else ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Type Selection
            Text(
                text = "How do you track it?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = HabitInkSoft
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TypeSelectChip(
                    label = "Yes / no",
                    isSelected = type == "tick",
                    onClick = { type = "tick" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("type_tick_chip")
                )
                TypeSelectChip(
                    label = "A number",
                    isSelected = type == "numeric",
                    onClick = { type = "numeric" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("type_numeric_chip")
                )
            }

            // Numeric Target Field
            if (type == "numeric") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daily target",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = HabitInkSoft
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            targetStr = it
                            if (errorMessage != null) errorMessage = null
                        }
                    },
                    placeholder = { Text("e.g. 8", color = HabitInkFaint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_target_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HabitMoss,
                        unfocusedBorderColor = HabitBorder,
                        focusedContainerColor = HabitSurfaceAlt,
                        unfocusedContainerColor = HabitSurfaceAlt,
                        focusedTextColor = HabitInk,
                        unfocusedTextColor = HabitInk
                    )
                )
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = HabitRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add Habit Submit Button
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Give the habit a name."
                        return@Button
                    }
                    val targetNum = if (type == "numeric") {
                        val parsed = targetStr.toIntOrNull()
                        if (parsed == null || parsed <= 0) {
                            errorMessage = "Set a daily target greater than 0."
                            return@Button
                        }
                        parsed
                    } else null

                    onAddHabit(trimmedName, type, targetNum)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_habit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HabitMoss,
                    contentColor = HabitSurface
                )
            ) {
                Text(
                    text = "Add habit",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TypeSelectChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) HabitGreenBg else HabitSurfaceAlt
    val borderColor = if (isSelected) HabitMoss else HabitBorder
    val textColor = if (isSelected) HabitMossDark else HabitInkSoft

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
