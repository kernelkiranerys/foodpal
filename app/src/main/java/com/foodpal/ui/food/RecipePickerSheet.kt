package com.foodpal.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import com.foodpal.data.db.RecipeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePickerSheet(
    recipes: List<RecipeEntity>,
    onDismiss: () -> Unit,
    onPick: (RecipeEntity, Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingRecipe by remember { mutableStateOf<RecipeEntity?>(null) }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add a recipe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (recipes.isEmpty()) {
                Text(
                    "You haven't saved any recipes yet. Build one under the Recipes tab.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(recipes, key = { it.id }) { recipe ->
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
    }

    pendingRecipe?.let { recipe ->
        PortionCountDialog(
            recipe = recipe,
            onDismiss = { pendingRecipe = null },
            onConfirm = { count ->
                onPick(recipe, count)
                pendingRecipe = null
            },
        )
    }
}

@Composable
internal fun PortionCountDialog(
    recipe: RecipeEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var countText by remember { mutableStateOf("1") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add \"${recipe.name}\"") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How many portions? (Recipe makes ${recipe.portions}.)")
                OutlinedTextField(
                    value = countText,
                    onValueChange = { s -> countText = s.filter { it.isDigit() } },
                    label = { Text("Portions") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            val n = countText.toIntOrNull() ?: 0
            TextButton(
                onClick = { onConfirm(n.coerceIn(1, recipe.portions)) },
                enabled = n > 0,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
