package com.foodpal.ui.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodpal.FoodPalApp
import com.foodpal.data.api.OffApiClient
import com.foodpal.data.api.OffFood
import com.foodpal.data.db.CustomFoodEntity
import com.foodpal.data.db.RecipeIngredientEntity
import com.foodpal.ui.barcode.BarcodeScanner
import com.foodpal.ui.food.AmountUnitDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientPickerSheet(
    offClient: OffApiClient,
    onDismiss: () -> Unit,
    onPick: (RecipeIngredientEntity) -> Unit,
) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: RecipePickerViewModel = viewModel(factory = RecipePickerViewModel.factory(app))
    val customFoods by viewModel.customFoods.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var offResults by remember { mutableStateOf<List<OffFood>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingOff by remember { mutableStateOf<OffFood?>(null) }
    var pendingCustom by remember { mutableStateOf<CustomFoodEntity?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    fun runSearch(q: String) {
        scope.launch {
            searching = true
            error = null
            try {
                offResults = offClient.search(q)
            } catch (e: Exception) {
                error = e.message ?: "Search failed"
                offResults = emptyList()
            } finally {
                searching = false
            }
        }
    }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            offResults = emptyList()
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(300)
        runSearch(query)
    }

    val filteredCustom = remember(query, customFoods) {
        if (query.isBlank()) customFoods
        else customFoods.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add ingredient", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search OFF database + your foods") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(4.dp))
                IconButton(onClick = { showScanner = true }) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan barcode")
                }
            }

            if (searching) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    Text("Searching…", style = MaterialTheme.typography.bodySmall)
                }
            }
            error?.let {
                Text("⚠ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            if (offResults.isNotEmpty()) {
                Text("Open Food Facts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 220.dp)) {
                    items(offResults, key = { "off-${it.barcode}-${it.name}" }) { food ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${food.brand.ifEmpty { "Generic" }} · ${food.calories.toInt()} kcal/100g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { pendingOff = food }) { Text("Add") }
                        }
                    }
                }
            }

            if (filteredCustom.isNotEmpty()) {
                Text("Your custom foods", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 180.dp)) {
                    items(filteredCustom, key = { "custom-${it.id}" }) { food ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${food.defaultAmount} ${food.unit} · ${food.calories.toInt()} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { pendingCustom = food }) { Text("Add") }
                        }
                    }
                }
            }
        }
    }

    pendingOff?.let { food ->
        AmountUnitDialog(
            initialAmount = if (food.pieceWeightG != null) 1.0 else 100.0,
            initialUnit = if (food.pieceWeightG != null) "piece" else "g",
            pieceWeightG = food.pieceWeightG,
            onDismiss = { pendingOff = null },
            onConfirm = { amount, unit ->
                onPick(food.toIngredient(amount, unit))
                pendingOff = null
            },
        )
    }
    pendingCustom?.let { food ->
        AmountUnitDialog(
            initialAmount = food.defaultAmount,
            initialUnit = food.unit,
            pieceWeightG = food.pieceWeightG ?: com.foodpal.data.model.PieceWeights.lookup(food.name),
            onDismiss = { pendingCustom = null },
            onConfirm = { amount, unit ->
                onPick(food.toIngredient(amount, unit))
                pendingCustom = null
            },
        )
    }

    BarcodeScanner(
        active = showScanner,
        onResult = { barcode ->
            showScanner = false
            scope.launch {
                searching = true
                error = null
                try {
                    val food = offClient.lookupBarcode(barcode)
                    if (food != null) {
                        pendingOff = food
                    } else {
                        error = "No product found for barcode $barcode"
                    }
                } catch (e: Exception) {
                    error = e.message ?: "Barcode lookup failed"
                } finally {
                    searching = false
                }
            }
        },
        onCancel = { showScanner = false },
    )
}

internal fun CustomFoodEntity.toIngredient(amount: Double, unit: String): RecipeIngredientEntity {
    val ratio = when {
        unit == this.unit -> if (defaultAmount > 0) amount / defaultAmount else 1.0
        unit == "piece" -> {
            val pieceG = pieceWeightG ?: com.foodpal.data.model.PieceWeights.lookup(name) ?: 100.0
            val userGrams = amount * pieceG
            val foodGrams = com.foodpal.ui.food.UnitConversions.toGrams(defaultAmount, this.unit)
            if (foodGrams > 0) userGrams / foodGrams else 1.0
        }
        else -> {
            val userGrams = com.foodpal.ui.food.UnitConversions.toGrams(amount, unit)
            val foodGrams = com.foodpal.ui.food.UnitConversions.toGrams(defaultAmount, this.unit)
            if (foodGrams > 0) userGrams / foodGrams else 1.0
        }
    }
    return RecipeIngredientEntity(
        recipeId = 0L,
        name = name,
        amount = amount,
        unit = unit,
        calories = calories * ratio,
        proteinG = proteinG * ratio,
        carbsG = carbsG * ratio,
        fatG = fatG * ratio,
        microsJson = microsJson,
        source = "custom",
        foodId = id,
        barcode = null,
    )
}

internal fun OffFood.toIngredient(amount: Double, unit: String): RecipeIngredientEntity {
    val per100 = when (unit) {
        "g", "ml" -> amount / 100.0
        "kg", "litre" -> amount * 10.0
        "oz" -> amount * 28.3495 / 100.0
        "lbs" -> amount * 453.592 / 100.0
        "floz" -> amount * 29.5735 / 100.0
        "piece" -> (amount * (pieceWeightG ?: 100.0)) / 100.0
        else -> amount / 100.0
    }
    val microsJson = if (micros.isEmpty()) "{}" else {
        val obj = org.json.JSONObject()
        for ((k, v) in micros) obj.put(k, v)
        obj.toString()
    }
    return RecipeIngredientEntity(
        recipeId = 0L,
        name = name,
        amount = amount,
        unit = unit,
        calories = calories * per100,
        proteinG = proteinG * per100,
        carbsG = carbsG * per100,
        fatG = fatG * per100,
        microsJson = microsJson,
        source = if (type == "off") "off" else "generic",
        foodId = null,
        barcode = barcode.takeIf { it.isNotEmpty() },
    )
}
