package com.foodpal.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodpal.FoodPalApp
import com.foodpal.data.FoodPalRepository
import com.foodpal.data.db.CustomFoodEntity
import com.foodpal.data.db.FoodEntryEntity
import com.foodpal.data.db.RecipeEntity
import com.foodpal.data.db.RecipeIngredientEntity
import com.foodpal.data.model.Meal
import com.foodpal.data.model.Micros
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FoodScreenState(
    val date: LocalDate = LocalDate.now(),
    val breakfast: List<FoodEntryEntity> = emptyList(),
    val lunch: List<FoodEntryEntity> = emptyList(),
    val dinner: List<FoodEntryEntity> = emptyList(),
    val snacks: List<FoodEntryEntity> = emptyList(),
    val customFoods: List<CustomFoodEntity> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
)

class FoodViewModel(private val repository: FoodPalRepository) : ViewModel() {

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<FoodScreenState> = combine(
        _date.flatMapLatest { d -> repository.observeEntries(d.toString()) },
        repository.observeCustomFoods(),
        repository.observeRecipes(),
        _date,
    ) { entries, customFoods, recipes, date ->
        FoodScreenState(
            date = date,
            breakfast = entries.filter { it.meal == Meal.BREAKFAST.key },
            lunch = entries.filter { it.meal == Meal.LUNCH.key },
            dinner = entries.filter { it.meal == Meal.DINNER.key },
            snacks = entries.filter { it.meal == Meal.SNACKS.key },
            customFoods = customFoods,
            recipes = recipes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodScreenState())

    fun setPreviousDay() { _date.value = _date.value.minusDays(1) }
    fun setNextDay() { _date.value = _date.value.plusDays(1) }
    fun setDate(date: LocalDate) { _date.value = date }

    fun addEntry(meal: Meal, entry: FoodEntryEntity) {
        viewModelScope.launch {
            repository.addFoodEntry(entry.copy(date = _date.value.toString(), meal = meal.key))
        }
    }

    fun deleteEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { repository.deleteFoodEntry(entry) }
    }

    fun copyYesterday(meal: Meal) {
        val from = _date.value.minusDays(1).toString()
        val to = _date.value.toString()
        viewModelScope.launch { repository.copyMealEntries(from, to, meal.key) }
    }

    fun copyFromDate(meal: Meal, source: LocalDate) {
        viewModelScope.launch {
            repository.copyMealEntries(source.toString(), _date.value.toString(), meal.key)
        }
    }

    /**
     * Loads a recipe's ingredients, sums them, divides by [recipe.portions] × [portions],
     * and writes a single FoodEntryEntity to the current date's [meal] log.
     */
    fun addRecipeToMeal(recipe: RecipeEntity, portions: Int, meal: Meal) {
        viewModelScope.launch {
            val ingredients = repository.getIngredients(recipe.id)
            if (ingredients.isEmpty()) return@launch
            val entry = recipe.toEntry(ingredients, portions)
            repository.addFoodEntry(entry.copy(date = _date.value.toString(), meal = meal.key))
        }
    }

    private fun RecipeEntity.toEntry(
        ingredients: List<RecipeIngredientEntity>,
        portionsRequested: Int,
    ): FoodEntryEntity {
        var cal = 0.0
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0
        var microsJson = "{}"
        for (i in ingredients) {
            cal += i.calories
            protein += i.proteinG
            carbs += i.carbsG
            fat += i.fatG
            microsJson = Micros.sum(microsJson, i.microsJson)
        }
        val factor = if (portions > 0 && portionsRequested > 0) {
            portionsRequested.toDouble() / portions
        } else 0.0
        val portionWord = if (portionsRequested == 1) "portion" else "portions"
        return FoodEntryEntity(
            date = "",
            meal = "",
            name = "$name (${portionsRequested}/${portions} $portionWord)",
            amount = portionsRequested.toDouble(),
            unit = "portion",
            calories = cal * factor,
            proteinG = protein * factor,
            carbsG = carbs * factor,
            fatG = fat * factor,
            microsJson = Micros.scale(microsJson, factor),
            source = "recipe",
            foodId = id,
        )
    }

    companion object {
        fun factory(app: FoodPalApp) = viewModelFactory {
            initializer { FoodViewModel(app.repository) }
        }
    }
}
