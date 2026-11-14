package com.foodpal.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodpal.FoodPalApp
import com.foodpal.data.db.FoodEntryEntity
import com.foodpal.data.model.Meal
import com.foodpal.ui.nav.FoodPalBottomBar
import com.foodpal.ui.nav.Routes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(onNavigate: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: FoodViewModel = viewModel(factory = FoodViewModel.factory(app))
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = viewModel::setPreviousDay) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous day")
                    }
                    Text(
                        state.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Medium,
                    )
                    IconButton(onClick = viewModel::setNextDay) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next day")
                    }
                }
            })
        },
        bottomBar = { FoodPalBottomBar(current = Routes.Food, onNavigate = onNavigate) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.padding(top = 4.dp))
            Meal.values().forEach { meal ->
                val entries = entriesFor(meal, state)
                MealCard(
                    meal = meal,
                    entries = entries,
                    customFoods = state.customFoods,
                    recipes = state.recipes,
                    offClient = app.offClient,
                    onAdd = { entry -> viewModel.addEntry(meal, entry) },
                    onDelete = viewModel::deleteEntry,
                    onCopyYesterday = { viewModel.copyYesterday(meal) },
                    onCopyFromDate = { source -> viewModel.copyFromDate(meal, source) },
                    onAddCustomFood = { onNavigate(Routes.MyFood) },
                    onAddRecipe = { recipe, portions -> viewModel.addRecipeToMeal(recipe, portions, meal) },
                )
            }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
}

private fun entriesFor(meal: Meal, state: FoodScreenState): List<FoodEntryEntity> = when (meal) {
    Meal.BREAKFAST -> state.breakfast
    Meal.LUNCH -> state.lunch
    Meal.DINNER -> state.dinner
    Meal.SNACKS -> state.snacks
}
