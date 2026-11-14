package com.foodpal.ui.myfood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.foodpal.FoodPalApp
import com.foodpal.data.db.CustomFoodEntity
import com.foodpal.data.model.FoodUnit
import com.foodpal.data.model.MicroNutrientKeys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomFoodScreen(
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: MyFoodViewModel = viewModel(factory = MyFoodViewModel.factory(app))

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(FoodUnit.GRAM.symbol) }
    var unitExpanded by remember { mutableStateOf(false) }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var saturated by remember { mutableStateOf("") }
    var poly by remember { mutableStateOf("") }
    var mono by remember { mutableStateOf("") }
    var trans by remember { mutableStateOf("") }
    var cholesterol by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }
    var potassium by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var vitA by remember { mutableStateOf("") }
    var vitC by remember { mutableStateOf("") }
    var calcium by remember { mutableStateOf("") }
    var iron by remember { mutableStateOf("") }
    var pieceWeight by remember { mutableStateOf("") }

    val canSave = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true

    Scaffold(topBar = { TopAppBar(title = { Text("Add custom food") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food name") },
                placeholder = { Text("my bread…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { s -> amount = s.filter { it.isDigit() || it == '.' } },
                label = { Text("Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    unitExpanded,
                    onDismissRequest = { unitExpanded = false },
                ) {
                    FoodUnit.values().forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u.symbol) },
                            onClick = { unit = u.symbol; unitExpanded = false },
                        )
                    }
                }
            }

            Text("Macros (per the amount above)", style = MaterialTheme.typography.titleSmall)
            DecimalField("Calories (kcal)", calories) { calories = it }
            DecimalField("Protein (g)", protein) { protein = it }
            DecimalField("Carbs (g)", carbs) { carbs = it }
            DecimalField("Fat (g)", fat) { fat = it }

            Text("Piece weight (optional)", style = MaterialTheme.typography.titleSmall)
            DecimalField("Grams per piece (for eggs, bananas, etc.)", pieceWeight) { pieceWeight = it }

            Text("Micronutrients (optional)", style = MaterialTheme.typography.titleSmall)
            DecimalField("Saturated fat (g)", saturated) { saturated = it }
            DecimalField("Polyunsaturated fat (g)", poly) { poly = it }
            DecimalField("Monounsaturated fat (g)", mono) { mono = it }
            DecimalField("Trans fat (g)", trans) { trans = it }
            DecimalField("Cholesterol (mg)", cholesterol) { cholesterol = it }
            DecimalField("Sodium (mg)", sodium) { sodium = it }
            DecimalField("Potassium (mg)", potassium) { potassium = it }
            DecimalField("Fiber (g)", fiber) { fiber = it }
            DecimalField("Sugar (g)", sugar) { sugar = it }
            DecimalField("Vitamin A (%DV)", vitA) { vitA = it }
            DecimalField("Vitamin C (%DV)", vitC) { vitC = it }
            DecimalField("Calcium (%DV)", calcium) { calcium = it }
            DecimalField("Iron (%DV)", iron) { iron = it }

            Button(
                onClick = {
                    if (!canSave) return@Button
                    val microsJson = buildMicrosJson(
                        saturated = saturated, poly = poly, mono = mono, trans = trans,
                        cholesterol = cholesterol, sodium = sodium, potassium = potassium,
                        fiber = fiber, sugar = sugar, vitA = vitA, vitC = vitC,
                        calcium = calcium, iron = iron,
                    )
                    viewModel.save(
                        CustomFoodEntity(
                            name = name.trim(),
                            defaultAmount = amount.toDouble(),
                            unit = unit,
                            calories = calories.toDoubleOrNull() ?: 0.0,
                            proteinG = protein.toDoubleOrNull() ?: 0.0,
                            carbsG = carbs.toDoubleOrNull() ?: 0.0,
                            fatG = fat.toDoubleOrNull() ?: 0.0,
                            microsJson = microsJson,
                            pieceWeightG = pieceWeight.toDoubleOrNull(),
                        ),
                    )
                    onBack()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save food") }
        }
    }
}

@Composable
private fun DecimalField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { s -> onChange(s.filter { it.isDigit() || it == '.' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun buildMicrosJson(
    saturated: String, poly: String, mono: String, trans: String,
    cholesterol: String, sodium: String, potassium: String,
    fiber: String, sugar: String,
    vitA: String, vitC: String, calcium: String, iron: String,
): String {
    val obj = org.json.JSONObject()
    fun add(key: String, v: String) {
        val d = v.toDoubleOrNull() ?: return
        if (d > 0.0) obj.put(key, d)
    }
    add(MicroNutrientKeys.SATURATED_FAT, saturated)
    add(MicroNutrientKeys.POLYUNSATURATED_FAT, poly)
    add(MicroNutrientKeys.MONOUNSATURATED_FAT, mono)
    add(MicroNutrientKeys.TRANS_FAT, trans)
    add(MicroNutrientKeys.CHOLESTEROL, cholesterol)
    add(MicroNutrientKeys.SODIUM, sodium)
    add(MicroNutrientKeys.POTASSIUM, potassium)
    add(MicroNutrientKeys.FIBER, fiber)
    add(MicroNutrientKeys.SUGAR, sugar)
    add(MicroNutrientKeys.VITAMIN_A, vitA)
    add(MicroNutrientKeys.VITAMIN_C, vitC)
    add(MicroNutrientKeys.CALCIUM, calcium)
    add(MicroNutrientKeys.IRON, iron)
    return obj.toString()
}
