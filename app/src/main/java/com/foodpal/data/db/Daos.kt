package com.foodpal.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles LIMIT 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert
    suspend fun insert(profile: ProfileEntity): Long

    @Update
    suspend fun update(profile: ProfileEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)
}

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY id")
    fun observeForDate(date: String): Flow<List<FoodEntryEntity>>

    @Insert
    suspend fun insert(entry: FoodEntryEntity): Long

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query(
        """
        INSERT INTO food_entries (date, meal, name, amount, unit, calories, proteinG, carbsG, fatG, microsJson, source, foodId)
        SELECT :toDate, meal, name, amount, unit, calories, proteinG, carbsG, fatG, microsJson, source, foodId
        FROM food_entries src
        WHERE src.date = :fromDate AND src.meal = :meal
        AND NOT EXISTS (
            SELECT 1 FROM food_entries dst
            WHERE dst.date = :toDate AND dst.meal = :meal
              AND dst.name = src.name
              AND dst.amount = src.amount
              AND dst.unit = src.unit
        )
        """,
    )
    suspend fun copyMealIntoDate(fromDate: String, toDate: String, meal: String)
}

@Dao
interface CustomFoodDao {
    @Query("SELECT * FROM custom_foods ORDER BY name")
    fun observeAll(): Flow<List<CustomFoodEntity>>

    @Insert
    suspend fun insert(food: CustomFoodEntity): Long

    @Delete
    suspend fun delete(food: CustomFoodEntity)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_entries WHERE date = :date")
    fun observeForDate(date: String): Flow<List<ExerciseEntryEntity>>

    @Insert
    suspend fun insert(entry: ExerciseEntryEntity): Long

    @Delete
    suspend fun delete(entry: ExerciseEntryEntity)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observe(id: Long): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun get(id: Long): RecipeEntity?

    @Insert
    suspend fun insert(recipe: RecipeEntity): Long

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY id")
    fun observe(recipeId: Long): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY id")
    suspend fun get(recipeId: Long): List<RecipeIngredientEntity>

    @Insert
    suspend fun insert(ingredient: RecipeIngredientEntity): Long

    @Delete
    suspend fun delete(ingredient: RecipeIngredientEntity)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteForRecipe(recipeId: Long)
}
