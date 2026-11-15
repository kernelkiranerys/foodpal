package com.foodpal.ui.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.foodpal.data.db.ProfileEntity
import com.foodpal.data.db.SettingsEntity
import com.foodpal.data.model.Goal
import com.foodpal.data.model.Lifestyle
import com.foodpal.ui.model.rememberRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(onDone: () -> Unit) {
    val repository = rememberRepository()
    val scope = rememberCoroutineScope()

    var nickname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }
    var lifestyle by remember { mutableStateOf(Lifestyle.SEDENTARY) }
    var goal by remember { mutableStateOf(Goal.GAIN_MUSCLE) }
    var lifestyleExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val numericOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    val decimalOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    val canSubmit = nickname.isNotBlank() && age.isNotBlank() && height.isNotBlank() &&
        weight.isNotBlank() && goalWeight.isNotBlank() && !saving

    Scaffold(topBar = { TopAppBar(title = { Text("Create your profile") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = numericOptions,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it.filter { c -> c.isDigit() } },
                label = { Text("Height (cm)") },
                singleLine = true,
                keyboardOptions = numericOptions,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
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
                    value = lifestyle.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lifestyle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lifestyleExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(lifestyleExpanded, onDismissRequest = { lifestyleExpanded = false }) {
                    Lifestyle.values().forEach { l ->
                        DropdownMenuItem(
                            text = { Text(l.label) },
                            onClick = { lifestyle = l; lifestyleExpanded = false },
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
                    value = goal.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Goal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(goalExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(goalExpanded, onDismissRequest = { goalExpanded = false }) {
                    Goal.values().forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.label) },
                            onClick = { goal = g; goalExpanded = false },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = goalWeight,
                onValueChange = { goalWeight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Goal weight (kg)") },
                singleLine = true,
                keyboardOptions = decimalOptions,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (!canSubmit) return@Button
                    saving = true
                    scope.launch {
                        repository.saveProfile(
                            ProfileEntity(
                                nickname = nickname.trim(),
                                age = age.toInt(),
                                heightCm = height.toInt(),
                                weightKg = weight.toDouble(),
                                lifestyle = lifestyle.name,
                                goal = goal.name,
                                goalWeightKg = goalWeight.toDouble(),
                            ),
                        )
                        repository.saveSettings(SettingsEntity.DEFAULTS)
                        onDone()
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (saving) "Saving…" else "Get started")
            }
        }
    }
}
