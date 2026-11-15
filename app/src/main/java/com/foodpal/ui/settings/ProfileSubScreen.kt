package com.foodpal.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.foodpal.data.model.Goal
import com.foodpal.data.model.Lifestyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSubScreen(
    profile: ProfileState?,
    onUpdate: ((ProfileState) -> ProfileState) -> Unit,
) {
    if (profile == null) {
        Text("No profile found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val numericOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    val decimalOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    var lifestyleExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }

    val currentLifestyle = Lifestyle.entries.find { it.name == profile.lifestyle } ?: Lifestyle.SEDENTARY
    val currentGoal = Goal.entries.find { it.name == profile.goal } ?: Goal.GAIN_MUSCLE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = profile.nickname,
            onValueChange = { v -> onUpdate { it.copy(nickname = v) } },
            label = { Text("Nickname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = if (profile.age == 0) "" else profile.age.toString(),
            onValueChange = { s ->
                val n = s.filter { it.isDigit() }.toIntOrNull() ?: 0
                onUpdate { it.copy(age = n) }
            },
            label = { Text("Age") },
            singleLine = true,
            keyboardOptions = numericOptions,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = if (profile.heightCm == 0) "" else profile.heightCm.toString(),
            onValueChange = { s ->
                val n = s.filter { it.isDigit() }.toIntOrNull() ?: 0
                onUpdate { it.copy(heightCm = n) }
            },
            label = { Text("Height (cm)") },
            singleLine = true,
            keyboardOptions = numericOptions,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = if (profile.weightKg == 0.0) "" else profile.weightKg.toString(),
            onValueChange = { s ->
                val v = s.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                onUpdate { it.copy(weightKg = v) }
            },
            label = { Text("Weight (kg)") },
            singleLine = true,
            keyboardOptions = decimalOptions,
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(
            expanded = lifestyleExpanded,
            onExpandedChange = { lifestyleExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = currentLifestyle.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Lifestyle") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lifestyleExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            androidx.compose.material3.DropdownMenu(
                expanded = lifestyleExpanded,
                onDismissRequest = { lifestyleExpanded = false },
            ) {
                Lifestyle.entries.forEach { l ->
                    DropdownMenuItem(
                        text = { Text(l.label) },
                        onClick = { onUpdate { it.copy(lifestyle = l.name) }; lifestyleExpanded = false },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = goalExpanded,
            onExpandedChange = { goalExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = currentGoal.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Goal") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(goalExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            androidx.compose.material3.DropdownMenu(
                expanded = goalExpanded,
                onDismissRequest = { goalExpanded = false },
            ) {
                Goal.entries.forEach { g ->
                    DropdownMenuItem(
                        text = { Text(g.label) },
                        onClick = { onUpdate { it.copy(goal = g.name) }; goalExpanded = false },
                    )
                }
            }
        }

        OutlinedTextField(
            value = if (profile.goalWeightKg == 0.0) "" else profile.goalWeightKg.toString(),
            onValueChange = { s ->
                val v = s.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                onUpdate { it.copy(goalWeightKg = v) }
            },
            label = { Text("Goal weight (kg)") },
            singleLine = true,
            keyboardOptions = decimalOptions,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
