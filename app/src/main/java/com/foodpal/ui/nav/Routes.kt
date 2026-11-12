package com.foodpal.ui.nav

object Routes {
    const val SignUp = "signup"
    const val Dashboard = "dashboard"
    const val Food = "food"
    const val MyFood = "myfood"
    const val MyFoodAdd = "myfood/add"
    const val Recipes = "recipes"
    const val RecipeAdd = "recipe/add"
    const val RecipeEdit = "recipe/{id}"
    fun recipeEdit(id: Long) = "recipe/$id"
    const val Settings = "settings"
}