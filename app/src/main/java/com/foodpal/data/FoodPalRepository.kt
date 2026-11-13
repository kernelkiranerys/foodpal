package com.foodpal.data

import com.foodpal.data.db.CustomFoodDao
import com.foodpal.data.db.ExerciseDao
import com.foodpal.data.db.FoodEntryDao
import com.foodpal.data.db.ProfileDao
import com.foodpal.data.db.ProfileEntity
import com.foodpal.data.db.RecipeDao
import com.foodpal.data.db.RecipeEntity
import com.foodpal.data.db.RecipeIngredientDao
import com.foodpal.data.db.RecipeIngredientEntity
import com.foodpal.data.db.SettingsDao
import com.foodpal.data.db.SettingsEntity
import kotlinx.coroutines.flow.Flow

class FoodPalRepository(
    private val profileDao: ProfileDao,
    private val settingsDao: SettingsDao,
    private val foodEntryDao: FoodEntryDao,
    private val customFoodDao: CustomFoodDao,
    private val exerciseDao: ExerciseDao,
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
) {
    fun observeProfile(): Flow<ProfileEntity?> = profileDao.observeProfile()
    suspend fun getProfile(): ProfileEntity? = profileDao.getProfile()
    suspend fun saveProfile(profile: ProfileEntity) = profileDao.insert(profile)
    suspend fun updateProfile(profile: ProfileEntity) = profileDao.update(profile)

    fun observeSettings(): Flow<SettingsEntity?> = settingsDao.observeSettings()
    suspend fun saveSettings(settings: SettingsEntity) = settingsDao.upsert(settings)

    fun observeEntries(date: String): Flow<List<com.foodpal.data.db.FoodEntryEntity>> =
        foodEntryDao.observeForDate(date)

    suspend fun addFoodEntry(entry: com.foodpal.data.db.FoodEntryEntity): Long =
        foodEntryDao.insert(entry)

    suspend fun deleteFoodEntry(entry: com.foodpal.data.db.FoodEntryEntity) =
        foodEntryDao.delete(entry)

    suspend fun copyMealEntries(fromDate: String, toDate: String, meal: String) {
        foodEntryDao.copyMealIntoDate(fromDate, toDate, meal)
    }

    fun observeCustomFoods(): Flow<List<com.foodpal.data.db.CustomFoodEntity>> =
        customFoodDao.observeAll()

    suspend fun addCustomFood(food: com.foodpal.data.db.CustomFoodEntity): Long =
        customFoodDao.insert(food)

    suspend fun deleteCustomFood(food: com.foodpal.data.db.CustomFoodEntity) =
        customFoodDao.delete(food)

    fun observeExercise(date: String): Flow<List<com.foodpal.data.db.ExerciseEntryEntity>> =
        exerciseDao.observeForDate(date)

    suspend fun addExercise(entry: com.foodpal.data.db.ExerciseEntryEntity): Long =
        exerciseDao.insert(entry)

    suspend fun deleteExercise(entry: com.foodpal.data.db.ExerciseEntryEntity) =
        exerciseDao.delete(entry)

    // ---- Recipes ----

    fun observeRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeAll()

    fun observeRecipe(id: Long): Flow<RecipeEntity?> = recipeDao.observe(id)

    fun observeIngredients(recipeId: Long): Flow<List<RecipeIngredientEntity>> =
        recipeIngredientDao.observe(recipeId)

    suspend fun saveRecipe(recipe: RecipeEntity, ingredients: List<RecipeIngredientEntity>): Long {
        val now = System.currentTimeMillis()
        val recipeId = if (recipe.id == 0L) {
            recipeDao.insert(recipe.copy(createdAt = now, updatedAt = now))
        } else {
            recipeDao.update(recipe.copy(updatedAt = now))
            recipeIngredientDao.deleteForRecipe(recipe.id)
            recipe.id
        }
        for (ingredient in ingredients) {
            recipeIngredientDao.insert(ingredient.copy(recipeId = recipeId))
        }
        return recipeId
    }

    suspend fun getRecipe(id: Long): RecipeEntity? = recipeDao.get(id)

    suspend fun getIngredients(recipeId: Long): List<RecipeIngredientEntity> =
        recipeIngredientDao.get(recipeId)

    suspend fun deleteRecipe(recipe: RecipeEntity) {
        recipeIngredientDao.deleteForRecipe(recipe.id)
        recipeDao.deleteById(recipe.id)
    }
}

