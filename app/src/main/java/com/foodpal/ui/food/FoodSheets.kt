package com.foodpal.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodpal.data.api.OffApiClient
import com.foodpal.data.api.OffFood
import com.foodpal.data.api.toEntry
import com.foodpal.data.db.CustomFoodEntity
import com.foodpal.data.db.FoodEntryEntity
import com.foodpal.data.db.RecipeEntity
import com.foodpal.data.model.FoodUnit
import com.foodpal.ui.barcode.BarcodeScanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchSheet(
    customFoods: List<CustomFoodEntity>,
    offClient: OffApiClient,
    onDismiss: () -> Unit,
    onPick: (FoodEntryEntity) -> Unit,
) {
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

    // Debounce-ish: search when query length >= 2 and user pauses.
    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            offResults = emptyList()
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(300)
        runSearch(query)
    }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Search food", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search Open Food Facts + your foods") },
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

            if (!searching && offResults.isEmpty() && query.trim().length >= 2 && error == null) {
                Text(
                    "No products found on Open Food Facts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (offResults.isNotEmpty()) {
                Text("Open Food Facts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 220.dp)) {
                    items(offResults, key = { it.barcode + it.name }) { food ->
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

            // Customer custom foods (matches by name substring, like before)
            val filteredCustom = remember(query, customFoods) {
                if (query.isBlank()) customFoods
                else customFoods.filter { it.name.contains(query.trim(), ignoreCase = true) }
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
                onPick(food.toEntry(amount, unit))
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
                onPick(food.toEntry(amount, unit))
                pendingCustom = null
            },
        )
    }

    BarcodeScanner(
        active = showScanner,
        onResult = { barcode ->
            showScanner = false
            // Look up the barcode via the OFF API; if found, prompt for amount.
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodsSheet(
    customFoods: List<CustomFoodEntity>,
    recipes: List<RecipeEntity>,
    onDismiss: () -> Unit,
    onPick: (CustomFoodEntity, Double, String) -> Unit,
    onAddCustomFood: () -> Unit,
    onAddRecipe: (RecipeEntity, Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pending by remember { mutableStateOf<CustomFoodEntity?>(null) }
    var pendingRecipe by remember { mutableStateOf<RecipeEntity?>(null) }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Your custom foods", style = MaterialTheme.typography.titleMedium)
            if (customFoods.isEmpty()) {
                Text(
                    "Here you can add custom foods if you cannot find them in our database, eg 'my bread'…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onAddCustomFood, modifier = Modifier.fillMaxWidth()) {
                    Text("Add custom food")
                }
            } else {
                customFoods.forEach { food ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(food.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${food.defaultAmount} ${food.unit} · ${food.calories.toInt()} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { pending = food }) { Text("Add") }
                    }
                }
            }

            if (recipes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Your recipes", style = MaterialTheme.typography.titleMedium)
                recipes.forEach { recipe ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Makes ${recipe.portions} portion${if (recipe.portions == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { pendingRecipe = recipe }) { Text("Add") }
                    }
                }
            }
        }
    }

    pending?.let { food ->
        AmountUnitDialog(
            initialAmount = food.defaultAmount,
            initialUnit = food.unit,
            pieceWeightG = food.pieceWeightG ?: com.foodpal.data.model.PieceWeights.lookup(food.name),
            onDismiss = { pending = null },
            onConfirm = { amount, unit ->
                onPick(food, amount, unit)
                pending = null
            },
        )
    }
    pendingRecipe?.let { recipe ->
        PortionCountDialog(
            recipe = recipe,
            onDismiss = { pendingRecipe = null },
            onConfirm = { count ->
                onAddRecipe(recipe, count)
                pendingRecipe = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountUnitDialog(
    initialAmount: Double,
    initialUnit: String,
    pieceWeightG: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit,
) {
    var amountText by remember { mutableStateOf(initialAmount.toString()) }
    var unit by remember { initialUnit }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Amount & unit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { s -> amountText = s.filter { it.isDigit() || it == '.' } },
                    label = { Text("Amount") },
                    singleLine = true,
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
                                text = {
                                    if (u == com.foodpal.data.model.FoodUnit.PIECE && pieceWeightG != null) {
                                        Text("${u.symbol} (1 ≈ ${pieceWeightG.toInt()}g)")
                                    } else {
                                        Text(u.symbol)
                                    }
                                },
                                onClick = {
                                    unit = u.symbol
                                    unitExpanded = false
                                    // Default to 1 piece if the current amount looks like a gram value
                                    if (u == com.foodpal.data.model.FoodUnit.PIECE) {
                                        val currentAmount = amountText.toDoubleOrNull() ?: 0.0
                                        if (currentAmount > 50) {
                                            // Looks like grams — switch to 1 piece
                                            amountText = "1"
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
                // Show piece-weight hint when "piece" is selected
                if (unit == "piece" && pieceWeightG != null) {
                    Text(
                        "1 piece ≈ ${pieceWeightG.toInt()} g (calories/macros scaled from this weight)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (unit == "piece" && pieceWeightG == null) {
                    Text(
                        "1 piece ≈ 100 g (default — override in My Foods for accuracy)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) onConfirm(amount, unit)
                },
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
