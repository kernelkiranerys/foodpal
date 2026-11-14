package com.foodpal.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.foodpal.data.db.ExerciseEntryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSheet(
    entries: List<ExerciseEntryEntity>,
    onAdd: (String, Int) -> Unit,
    onDelete: (ExerciseEntryEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Exercise", style = MaterialTheme.typography.titleMedium)

            if (entries.isEmpty()) {
                Text(
                    "No exercise logged today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${entry.caloriesBurned} kcal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { onDelete(entry) }) { Text("Remove") }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(Modifier.padding(top = 4.dp))
            Text("Add exercise", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Activity (e.g. Running)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = calories,
                onValueChange = { s -> calories = s.filter { it.isDigit() } },
                label = { Text("Calories burned") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    val burned = calories.toIntOrNull() ?: 0
                    if (name.isNotBlank() && burned > 0) {
                        onAdd(name, burned)
                        name = ""
                        calories = ""
                    }
                },
                enabled = name.isNotBlank() && calories.toIntOrNull()?.let { it > 0 } == true,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Add") }
        }
    }
}
