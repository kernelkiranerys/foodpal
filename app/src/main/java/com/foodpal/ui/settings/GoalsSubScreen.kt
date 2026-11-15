package com.foodpal.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun GoalsSubScreen(
    state: SettingsState,
    scrollTarget: String?,
    onUpdate: ((SettingsState) -> SettingsState) -> Unit,
) {
    val scrollState = rememberScrollState()
    var macrosOffset by remember { mutableIntStateOf(0) }
    var microsOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollTarget, macrosOffset, microsOffset) {
        val target = when (scrollTarget) {
            "macros" -> macrosOffset
            "micros" -> microsOffset
            else -> 0
        }
        if (target > 0) scrollState.animateScrollTo(target)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daily calorie goal", style = MaterialTheme.typography.titleMedium)
                IntField("Calories", state.goalCalories) { v ->
                    onUpdate { it.copy(goalCalories = v) }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { macrosOffset = it.positionInRoot().y.toInt() },
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Macros (% of calories)", style = MaterialTheme.typography.titleMedium)
                IntField("Protein %", state.proteinPct) { v -> onUpdate { it.copy(proteinPct = v.coerceIn(0, 100)) } }
                IntField("Carbs %", state.carbsPct) { v -> onUpdate { it.copy(carbsPct = v.coerceIn(0, 100)) } }
                IntField("Fat %", state.fatPct) { v -> onUpdate { it.copy(fatPct = v.coerceIn(0, 100)) } }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { microsOffset = it.positionInRoot().y.toInt() },
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Micronutrient goals", style = MaterialTheme.typography.titleMedium)
                DoubleField("Saturated fat (g)", state.saturatedFatG) { v -> onUpdate { it.copy(saturatedFatG = v) } }
                DoubleField("Polyunsaturated fat (g)", state.polyunsaturatedFatG) { v -> onUpdate { it.copy(polyunsaturatedFatG = v) } }
                DoubleField("Monounsaturated fat (g)", state.monounsaturatedFatG) { v -> onUpdate { it.copy(monounsaturatedFatG = v) } }
                DoubleField("Trans fat (g)", state.transFatG) { v -> onUpdate { it.copy(transFatG = v) } }
                DoubleField("Cholesterol (mg)", state.cholesterolMg) { v -> onUpdate { it.copy(cholesterolMg = v) } }
                DoubleField("Sodium (mg)", state.sodiumMg) { v -> onUpdate { it.copy(sodiumMg = v) } }
                DoubleField("Potassium (mg)", state.potassiumMg) { v -> onUpdate { it.copy(potassiumMg = v) } }
                DoubleField("Fiber (g)", state.fiberG) { v -> onUpdate { it.copy(fiberG = v) } }
                DoubleField("Sugar (g)", state.sugarG) { v -> onUpdate { it.copy(sugarG = v) } }
                IntField("Vitamin A (%DV)", state.vitaminADvPct) { v -> onUpdate { it.copy(vitaminADvPct = v) } }
                IntField("Vitamin C (%DV)", state.vitaminCDvPct) { v -> onUpdate { it.copy(vitaminCDvPct = v) } }
                IntField("Calcium (%DV)", state.calciumDvPct) { v -> onUpdate { it.copy(calciumDvPct = v) } }
                IntField("Iron (%DV)", state.ironDvPct) { v -> onUpdate { it.copy(ironDvPct = v) } }
            }
        }
    }
}

@Composable
internal fun IntField(label: String, value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { androidx.compose.runtime.mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { s ->
            text = s.filter { it.isDigit() }
            text.toIntOrNull()?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun DoubleField(label: String, value: Double, onChange: (Double) -> Unit) {
    var text by remember(value) { androidx.compose.runtime.mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { s ->
            text = s.filter { it.isDigit() || it == '.' }
            text.toDoubleOrNull()?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}
