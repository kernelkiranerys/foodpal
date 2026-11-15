package com.foodpal.ui.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.foodpal.FoodPalApp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import com.foodpal.data.db.RecipeIngredientEntity
import com.foodpal.data.model.Micros

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBuilderScreen(
    recipeId: Long,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: RecipeBuilderViewModel = viewModel(factory = RecipeBuilderViewModel.factory(app, recipeId))
    val name by viewModel.name.collectAsStateWithLifecycle()
    val portions by viewModel.portions.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showPicker by remember { mutableStateOf(false) }
    var pendingEdit by remember { mutableStateOf<RecipeIngredientEntity?>(null) }

    // Local buffer for the portions text field. Keeps the field editable
    // even mid-edit (e.g. clearing the value to type a new one) without
    // the VM forcing a re-render back to the last valid integer.
    var portionsText by remember(portions) { mutableStateOf(portions.toString()) }

    val perPortion = totals.perPortion(portions)
    val canSave = name.isNotBlank() && ingredients.isNotEmpty()

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId == 0L) "New recipe" else "Edit recipe") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::setName,
                label = { Text("Recipe name") },
                placeholder = { Text("e.g. Chilli con carne…") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = portionsText,
                    onValueChange = { s ->
                        portionsText = s.filter { it.isDigit() }
                        portionsText.toIntOrNull()?.let { n ->
                            if (n != portions) viewModel.setPortions(n)
                        }
                    },
                    label = { Text("Portions") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }),
                    modifier = Modifier.fillMaxWidth(0.4f),
                )
                Text(
                    "Makes $portions portion${if (portions == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (ingredients.isEmpty()) {
                Text(
                    "No ingredients added yet. Tap + to add from your custom foods or search the Open Food Facts database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ingredients.forEach { ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onEdit = { pendingEdit = ingredient },
                        onRemove = { viewModel.removeIngredient(ingredient) },
                    )
                    HorizontalDivider()
                }
            }

            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Add ingredient")
            }

            Spacer(Modifier.padding(top = 8.dp))

            TotalsCard(
                title = "Per recipe (${ingredients.size} ingredients)",
                calories = totals.calories,
                protein = totals.proteinG,
                carbs = totals.carbsG,
                fat = totals.fatG,
                microsJson = totals.microsJson,
            )
            TotalsCard(
                title = "Per portion (1 of $portions)",
                calories = perPortion.calories,
                protein = perPortion.proteinG,
                carbs = perPortion.carbsG,
                fat = perPortion.fatG,
                microsJson = perPortion.microsJson,
            )

            Button(
                onClick = viewModel::save,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save recipe") }

            Spacer(Modifier.padding(bottom = 24.dp))
        }
    }

    if (showPicker) {
        IngredientPickerSheet(
            offClient = app.offClient,
            onDismiss = { showPicker = false },
            onPick = { ingredient ->
                viewModel.addIngredient(ingredient)
                showPicker = false
            },
        )
    }

    pendingEdit?.let { ingredient ->
        EditIngredientDialog(
            ingredient = ingredient,
            onDismiss = { pendingEdit = null },
            onConfirm = { updated ->
                viewModel.replaceIngredient(ingredient, updated)
                pendingEdit = null
            },
        )
    }
}

@Composable
private fun IngredientRow(
    ingredient: RecipeIngredientEntity,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ingredient.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${ingredient.amount} ${ingredient.unit} · ${ingredient.calories.toInt()} kcal · " +
                    "P ${ingredient.proteinG.toInt()}g C ${ingredient.carbsG.toInt()}g F ${ingredient.fatG.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(onClick = onEdit) { Text("Edit") }
        Spacer(Modifier.padding(4.dp))
        OutlinedButton(onClick = onRemove) { Text("Remove") }
    }
}

@Composable
private fun TotalsCard(
    title: String,
    calories: Double,
    protein: Double,
    carbs: Double,
    fat: Double,
    microsJson: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("${calories.toInt()} kcal", style = MaterialTheme.typography.bodyLarge)

            MacroRow("Protein", protein, 50.0)
            MacroRow("Carbs", carbs, 100.0)
            MacroRow("Fat", fat, 50.0)

            val micros = Micros.parse(microsJson)
            if (micros.isNotEmpty()) {
                Spacer(Modifier.padding(top = 4.dp))
                Text("Micronutrients", style = MaterialTheme.typography.labelMedium)
                micros.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((k, v) in row) {
                            AssistChip(onClick = {}, label = { Text("$k ${v.toInt()}") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroRow(label: String, value: Double, referenceForBar: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${value.toInt()} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val progress = if (referenceForBar > 0) (value / referenceForBar).toFloat().coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIngredientDialog(
    ingredient: RecipeIngredientEntity,
    onDismiss: () -> Unit,
    onConfirm: (RecipeIngredientEntity) -> Unit,
) {
    var amountText by remember { mutableStateOf(ingredient.amount.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Currently: ${ingredient.amount} ${ingredient.unit}")
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { s -> amountText = s.filter { it.isDigit() || it == '.' } },
                    label = { Text("New amount (${ingredient.unit})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newAmount = amountText.toDoubleOrNull() ?: return@TextButton
                if (newAmount <= 0) return@TextButton
                val ratio = if (ingredient.amount > 0) newAmount / ingredient.amount else 1.0
                onConfirm(
                    ingredient.copy(
                        amount = newAmount,
                        calories = ingredient.calories * ratio,
                        proteinG = ingredient.proteinG * ratio,
                        carbsG = ingredient.carbsG * ratio,
                        fatG = ingredient.fatG * ratio,
                        microsJson = Micros.scale(ingredient.microsJson, ratio),
                    ),
                )
            }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
