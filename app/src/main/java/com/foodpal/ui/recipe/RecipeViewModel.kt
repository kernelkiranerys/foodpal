package com.foodpal.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodpal.FoodPalApp
import com.foodpal.data.FoodPalRepository
import com.foodpal.data.db.RecipeEntity
import com.foodpal.data.db.RecipeIngredientEntity
import com.foodpal.data.model.Micros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeTotals(
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val microsJson: String,
) {
    companion object {
        val ZERO = RecipeTotals(0.0, 0.0, 0.0, 0.0, "{}")

        fun from(ingredients: List<RecipeIngredientEntity>): RecipeTotals {
            var calories = 0.0
            var protein = 0.0
            var carbs = 0.0
            var fat = 0.0
            var micros = "{}"
            for (i in ingredients) {
                calories += i.calories
                protein += i.proteinG
                carbs += i.carbsG
                fat += i.fatG
                micros = Micros.sum(micros, i.microsJson)
            }
            return RecipeTotals(calories, protein, carbs, fat, micros)
        }
    }

    fun perPortion(portions: Int): RecipeTotals {
        if (portions <= 1) return this
        val factor = 1.0 / portions
        return RecipeTotals(
            calories = calories * factor,
            proteinG = proteinG * factor,
            carbsG = carbsG * factor,
            fatG = fatG * factor,
            microsJson = Micros.scale(microsJson, factor),
        )
    }
}

class RecipeViewModel(private val repository: FoodPalRepository) : ViewModel() {

    val recipes: StateFlow<List<RecipeEntity>> = repository.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(recipe: RecipeEntity) {
        viewModelScope.launch { repository.deleteRecipe(recipe) }
    }

    companion object {
        fun factory(app: FoodPalApp) = viewModelFactory {
            initializer { RecipeViewModel(app.repository) }
        }
    }
}

/**
 * ViewModel for the recipe builder screen. Holds editable recipe state
 * (name, portions, ingredients) in memory and persists on save.
 */
class RecipeBuilderViewModel(
    private val repository: FoodPalRepository,
    private val recipeId: Long,  // 0 = new recipe
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _portions = MutableStateFlow(1)
    val portions: StateFlow<Int> = _portions.asStateFlow()

    private val _ingredients = MutableStateFlow<List<RecipeIngredientEntity>>(emptyList())
    val ingredients: StateFlow<List<RecipeIngredientEntity>> = _ingredients.asStateFlow()

    val totals: StateFlow<RecipeTotals> = _ingredients
        .map { RecipeTotals.from(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeTotals.ZERO)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        if (recipeId != 0L) {
            viewModelScope.launch {
                repository.getRecipe(recipeId)?.let { recipe ->
                    _name.value = recipe.name
                    _portions.value = recipe.portions
                }
                repository.getIngredients(recipeId).let { ingredients ->
                    _ingredients.value = ingredients
                }
            }
        }
    }

    fun setName(value: String) { _name.value = value }
    fun setPortions(value: Int) { _portions.value = value.coerceIn(1, 100) }

    fun addIngredient(ingredient: RecipeIngredientEntity) {
        _ingredients.value = _ingredients.value + ingredient
    }

    fun removeIngredient(ingredient: RecipeIngredientEntity) {
        _ingredients.value = _ingredients.value.filterNot { it === ingredient || it.id == ingredient.id && ingredient.id != 0L }
    }

    fun replaceIngredient(old: RecipeIngredientEntity, new: RecipeIngredientEntity) {
        _ingredients.value = _ingredients.value.map { if (it === old) new else it }
    }

    fun save() {
        val name = _name.value.trim()
        if (name.isBlank() || _ingredients.value.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recipe = RecipeEntity(
                id = recipeId,
                name = name,
                portions = _portions.value,
                createdAt = if (recipeId == 0L) now else 0L,  // repo.saveRecipe overrides
                updatedAt = now,
            )
            repository.saveRecipe(recipe, _ingredients.value)
            _saved.value = true
        }
    }

    companion object {
        fun factory(app: FoodPalApp, recipeId: Long) = viewModelFactory {
            initializer { RecipeBuilderViewModel(app.repository, recipeId) }
        }
    }
}

/**
 * Tiny ViewModel used by [IngredientPickerSheet] to expose the custom foods list.
 */
class RecipePickerViewModel(
    repository: FoodPalRepository,
) : ViewModel() {
    val customFoods: StateFlow<List<com.foodpal.data.db.CustomFoodEntity>> =
        repository.observeCustomFoods()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(app: FoodPalApp) = viewModelFactory {
            initializer { RecipePickerViewModel(app.repository) }
        }
    }
}
