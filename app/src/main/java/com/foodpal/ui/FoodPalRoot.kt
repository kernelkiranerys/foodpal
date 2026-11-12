package com.foodpal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foodpal.FoodPalApp
import com.foodpal.ui.dashboard.DashboardScreen
import com.foodpal.ui.food.FoodScreen
import com.foodpal.ui.myfood.AddCustomFoodScreen
import com.foodpal.ui.myfood.MyFoodScreen
import com.foodpal.ui.nav.Routes
import com.foodpal.ui.recipe.RecipeBuilderScreen
import com.foodpal.ui.recipe.RecipeListScreen
import com.foodpal.ui.settings.SettingsScreen
import com.foodpal.ui.signup.SignUpScreen
import com.foodpal.ui.theme.FoodPalTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FoodPalRoot() {
    FoodPalTheme {
        val app = LocalContext.current.applicationContext as FoodPalApp
        var startDestination by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            startDestination = withContext(Dispatchers.IO) {
                if (app.repository.getProfile() == null) Routes.SignUp else Routes.Dashboard
            }
        }

        val resolved = startDestination
        if (resolved == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            FoodPalNavGraph(startDestination = resolved)
        }
    }
}

@Composable
private fun FoodPalNavGraph(startDestination: String) {
    val navController = rememberNavController()
    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.Dashboard) { saveState = true; inclusive = false }
            launchSingleTop = true
            restoreState = true
        }
    }
    val openSettings: (String) -> Unit = { section ->
        navController.navigate("${Routes.Settings}?section=$section")
    }
    val popBack: () -> Unit = { navController.popBackStack() }

    NavHost(navController, startDestination = startDestination) {
        composable(Routes.SignUp) {
            SignUpScreen(
                onDone = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.SignUp) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Dashboard) {
            DashboardScreen(
                onNavigate = navigateToTab,
                onEditMacros = { openSettings("macros") },
                onEditMicros = { openSettings("micros") },
                onOpenSettings = { openSettings("") },
            )
        }
        composable(Routes.Food) {
            FoodScreen(onNavigate = navigateToTab)
        }
        composable(Routes.MyFood) {
            MyFoodScreen(
                onNavigate = navigateToTab,
                onAddFood = { navController.navigate(Routes.MyFoodAdd) },
            )
        }
        composable(Routes.MyFoodAdd) {
            AddCustomFoodScreen(onBack = popBack)
        }
        composable(Routes.Recipes) {
            RecipeListScreen(
                onNavigate = navigateToTab,
                onAddRecipe = { navController.navigate(Routes.RecipeAdd) },
                onEditRecipe = { id -> navController.navigate(Routes.recipeEdit(id)) },
            )
        }
        composable(Routes.RecipeAdd) {
            RecipeBuilderScreen(recipeId = 0L, onBack = popBack)
        }
        composable(
            route = Routes.RecipeEdit,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            RecipeBuilderScreen(recipeId = id, onBack = popBack)
        }
        composable(
            route = "${Routes.Settings}?section={section}",
            arguments = listOf(navArgument("section") { type = NavType.StringType; defaultValue = "" }),
        ) { backStackEntry ->
            val section = backStackEntry.arguments?.getString("section").orEmpty()
            SettingsScreen(section = section, onBack = popBack)
        }
    }
}
