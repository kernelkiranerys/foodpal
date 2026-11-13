package com.foodpal.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val lifestyle: String,
    val goal: String,
    val goalWeightKg: Double,
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Long = 1,
    val goalCalories: Int,
    val proteinPct: Int,
    val carbsPct: Int,
    val fatPct: Int,
    val saturatedFatG: Double,
    val polyunsaturatedFatG: Double,
    val monounsaturatedFatG: Double,
    val transFatG: Double,
    val cholesterolMg: Double,
    val sodiumMg: Double,
    val potassiumMg: Double,
    val fiberG: Double,
    val sugarG: Double,
    val vitaminADvPct: Int,
    val vitaminCDvPct: Int,
    val calciumDvPct: Int,
    val ironDvPct: Int,
) {
    companion object {
        val DEFAULTS = SettingsEntity(
            id = 1,
            goalCalories = 2000,
            proteinPct = 30,
            carbsPct = 40,
            fatPct = 30,
            saturatedFatG = 32.0,
            polyunsaturatedFatG = 0.0,
            monounsaturatedFatG = 0.0,
            transFatG = 0.0,
            cholesterolMg = 300.0,
            sodiumMg = 2300.0,
            potassiumMg = 3500.0,
            fiberG = 38.0,
            sugarG = 108.0,
            vitaminADvPct = 100,
            vitaminCDvPct = 100,
            calciumDvPct = 100,
            ironDvPct = 100,
        )
    }
}

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val meal: String,
    val name: String,
    val amount: Double,
    val unit: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val microsJson: String,
    val source: String,
    val foodId: Long? = null,
)

@Entity(tableName = "custom_foods")
data class CustomFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultAmount: Double,
    val unit: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val microsJson: String,
    val pieceWeightG: Double? = null,  // grams per piece when unit "piece" is used; null = unknown
)

@Entity(tableName = "exercise_entries")
data class ExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val name: String,
    val caloriesBurned: Int,
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val portions: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val amount: Double,
    val unit: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val microsJson: String,
    val source: String,        // "custom" | "off" | "manual"
    val foodId: Long? = null,  // FK to custom_foods.id when source == "custom"
    val barcode: String? = null, // OFF barcode when source == "off"
)
