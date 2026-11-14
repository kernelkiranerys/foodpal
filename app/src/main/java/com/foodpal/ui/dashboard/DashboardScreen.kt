package com.foodpal.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodpal.FoodPalApp
import com.foodpal.ui.nav.FoodPalBottomBar
import com.foodpal.ui.nav.Routes
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onEditMacros: () -> Unit,
    onEditMicros: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(app))
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showExercise by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            FoodPalBottomBar(current = Routes.Dashboard, onNavigate = onNavigate)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CaloriesCard(state, onOpenExercise = { showExercise = true })
            MacrosCard(state, onEditMacros)
            MicrosCard(state, onEditMicros)
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showExercise) {
        ExerciseSheet(
            entries = state.exerciseEntries,
            onAdd = { name, kcal -> viewModel.addExercise(name, kcal) },
            onDelete = { entry -> viewModel.deleteExercise(entry) },
            onDismiss = { showExercise = false },
        )
    }
}

@Composable
private fun CaloriesCard(state: DashboardState, onOpenExercise: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenExercise,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Calories", style = MaterialTheme.typography.titleMedium)
            Text(
                "${state.caloriesConsumed} kcal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Exercise: ${state.exerciseCalories} kcal", style = MaterialTheme.typography.bodyMedium)
                Text("Goal: ${state.goalCalories} kcal", style = MaterialTheme.typography.bodyMedium)
            }
            val ratio = if (state.goalCalories > 0) {
                (state.caloriesConsumed.toFloat() / state.goalCalories).coerceIn(0f, 1f)
            } else 0f
            LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
            Text(
                "Tap to log exercise →",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MacrosCard(state: DashboardState, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Macros", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onEdit) { Text("Edit macros") }
            }
            MacroBar("Protein", state.proteinG, state.proteinGoalG, "g")
            MacroBar("Carbs", state.carbsG, state.carbsGoalG, "g")
            MacroBar("Fat", state.fatG, state.fatGoalG, "g")
        }
    }
}

@Composable
private fun MacroBar(name: String, consumed: Double, goal: Double, unit: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${consumed.toInt()} / ${goal.toInt()} $unit",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        val ratio = if (goal > 0) (consumed / goal).toFloat().coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun MicrosCard(state: DashboardState, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Micronutrients", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onEdit) { Text("Edit micronutrients") }
            }
            state.micros.forEach { stat ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stat.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${stat.consumed.toInt()} / ${stat.goal.toInt()} ${stat.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    val ratio = if (stat.goal > 0) {
                        (stat.consumed / stat.goal).toFloat().coerceIn(0f, 1f)
                    } else 0f
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
