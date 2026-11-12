package com.foodpal

import android.app.Application
import com.foodpal.data.FoodPalRepository
import com.foodpal.data.api.OffApiClient
import com.foodpal.data.api.UidManager
import com.foodpal.data.db.FoodPalDatabase

class FoodPalApp : Application() {
    val database by lazy { FoodPalDatabase.get(this) }
    val repository by lazy {
        FoodPalRepository(
            profileDao = database.profileDao(),
            settingsDao = database.settingsDao(),
            foodEntryDao = database.foodEntryDao(),
            customFoodDao = database.customFoodDao(),
            exerciseDao = database.exerciseDao(),
            recipeDao = database.recipeDao(),
            recipeIngredientDao = database.recipeIngredientDao(),
        )
    }
    val uidManager by lazy { UidManager(this) }
    val offClient by lazy { OffApiClient(uidManager = uidManager) }
}
