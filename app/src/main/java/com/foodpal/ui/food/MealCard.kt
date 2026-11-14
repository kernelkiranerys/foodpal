package com.foodpal.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodpal.FoodPalApp
import com.foodpal.data.api.OffApiClient
import com.foodpal.data.db.CustomFoodEntity
import com.foodpal.data.db.FoodEntryEntity
import com.foodpal.data.db.RecipeEntity
import com.foodpal.data.model.Meal
import java.time.LocalDate

@Composable
fun MealCard(
    meal: Meal,
    entries: List<FoodEntryEntity>,
    customFoods: List<CustomFoodEntity>,
    recipes: List<RecipeEntity>,
    offClient: OffApiClient,
    onAdd: (FoodEntryEntity) -> Unit,
    onDelete: (FoodEntryEntity) -> Unit,
    onCopyYesterday: () -> Unit,
    onCopyFromDate: (LocalDate) -> Unit,
    onAddCustomFood: () -> Unit,
    onAddRecipe: (RecipeEntity, Int) -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as FoodPalApp }
    var menuOpen by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    var showCopyFrom by remember { mutableStateOf(false) }
    var showRecipes by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(meal.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Text(
                    "${entries.sumOf { it.calories }.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Copy yesterday") },
                        onClick = { menuOpen = false; onCopyYesterday() },
                    )
                    DropdownMenuItem(
                        text = { Text("Copy from date…") },
                        onClick = { menuOpen = false; showCopyFrom = true },
                    )
                    DropdownMenuItem(
                        text = { Text("Add recipe…") },
                        onClick = { menuOpen = false; showRecipes = true },
                    )
                }
            }

            if (entries.isEmpty()) {
                Text(
                    "Nothing logged yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                entries.forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${entry.amount} ${entry.unit} · ${entry.calories.toInt()} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { onDelete(entry) }) { Text("Remove") }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showSearch = true }, modifier = Modifier.weight(1f)) {
                    Text("Search food")
                }
                OutlinedButton(onClick = { showCustom = true }, modifier = Modifier.weight(1f)) {
                    Text("Add custom food")
                }
            }
        }
    }

    if (showSearch) {
        FoodSearchSheet(
            customFoods = customFoods,
            offClient = offClient,
            onDismiss = { showSearch = false },
            onPick = { entry ->
                onAdd(entry)
                showSearch = false
            },
        )
    }
    if (showCustom) {
        CustomFoodsSheet(
            customFoods = customFoods,
            recipes = recipes,
            onDismiss = { showCustom = false },
            onPick = { food, amount, unit ->
                onAdd(food.toEntry(amount, unit))
                showCustom = false
            },
            onAddCustomFood = {
                showCustom = false
                onAddCustomFood()
            },
            onAddRecipe = { recipe, portions ->
                onAddRecipe(recipe, portions)
                showCustom = false
            },
        )
    }
    if (showCopyFrom) {
        CopyFromDateDialog(
            onDismiss = { showCopyFrom = false },
            onConfirm = { date ->
                showCopyFrom = false
                onCopyFromDate(date)
            },
        )
    }
    if (showRecipes) {
        RecipePickerSheet(
            recipes = recipes,
            onDismiss = { showRecipes = false },
            onPick = { recipe, portions ->
                onAddRecipe(recipe, portions)
                showRecipes = false
            },
        )
    }
}

@Composable
fun CopyFromDateDialog(onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now().minusDays(1)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copy from date") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Source date: $date")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { date = date.minusDays(1) }) { Text("-1d") }
                    TextButton(onClick = { date = date.plusDays(1) }) { Text("+1d") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(date) }) { Text("Copy") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

internal fun CustomFoodEntity.toEntry(amount: Double, unit: String): FoodEntryEntity {
    // Convert the user's amount+unit to the same unit basis as the food's defaultAmount+unit.
    // For "piece", use pieceWeightG (if set) to convert to grams first.
    val ratio = when {
        unit == this.unit -> {
            // Same unit as the food's native unit — direct ratio.
            if (defaultAmount > 0) amount / defaultAmount else 1.0
        }
        unit == "piece" -> {
            // User wants pieces; convert via pieceWeightG (or PieceWeights lookup, or 100g fallback).
            val pieceG = pieceWeightG ?: com.foodpal.data.model.PieceWeights.lookup(name) ?: 100.0
            val userGrams = amount * pieceG
            val foodGrams = when (this.unit) {
                "g" -> defaultAmount
                "kg" -> defaultAmount * 1000
                "oz" -> defaultAmount * 28.3495
                "ml" -> defaultAmount
                "litre" -> defaultAmount * 1000
                else -> defaultAmount
            }
            if (foodGrams > 0) userGrams / foodGrams else 1.0
        }
        else -> {
            // Different weight/volume units — convert both to grams and ratio.
            val userGrams = UnitConversions.toGrams(amount, unit)
            val foodGrams = UnitConversions.toGrams(defaultAmount, this.unit)
            if (foodGrams > 0) userGrams / foodGrams else 1.0
        }
    }
    return FoodEntryEntity(
        date = "",
        meal = "",
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
    )
}

/** Gram conversion helpers shared across entry-building paths. */
object UnitConversions {
    fun toGrams(amount: Double, unit: String): Double = when (unit) {
        "g" -> amount
        "kg" -> amount * 1000
        "oz" -> amount * 28.3495
        "lbs" -> amount * 453.592
        "ml" -> amount       // 1ml ≈ 1g for water-like foods
        "litre" -> amount * 1000
        "floz" -> amount * 29.5735
        "piece" -> amount * 100.0  // fallback when pieceWeightG unknown
        else -> amount
    }
}
