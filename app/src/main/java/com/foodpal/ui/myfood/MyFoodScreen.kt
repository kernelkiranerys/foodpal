package com.foodpal.ui.myfood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.foodpal.FoodPalApp
import com.foodpal.ui.nav.FoodPalBottomBar
import com.foodpal.ui.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFoodScreen(
    onNavigate: (String) -> Unit,
    onAddFood: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: MyFoodViewModel = viewModel(factory = MyFoodViewModel.factory(app))
    val foods by viewModel.foods.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Foods") }) },
        bottomBar = { FoodPalBottomBar(current = Routes.MyFood, onNavigate = onNavigate) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (foods.isEmpty()) {
                Text(
                    "Here you can add custom foods if you cannot find them in our database, eg 'my bread'…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(foods, key = { it.id }) { food ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${food.defaultAmount} ${food.unit} · ${food.calories.toInt()} kcal · P ${food.proteinG.toInt()}g C ${food.carbsG.toInt()}g F ${food.fatG.toInt()}g",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { viewModel.delete(food) }) { Text("Delete") }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
            Button(onClick = onAddFood, modifier = Modifier.fillMaxWidth()) { Text("Add food") }
            Spacer(Modifier.padding(bottom = 8.dp))
        }
    }
}
