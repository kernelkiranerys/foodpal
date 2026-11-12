package com.foodpal.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FoodPalBottomBar(current: String, onNavigate: (String) -> Unit) {
    NavigationBar {
        listOf(
            Triple(Routes.Dashboard, "Today", Icons.Filled.Dashboard),
            Triple(Routes.Food, "Food", Icons.Filled.Restaurant),
            Triple(Routes.MyFood, "My Food", Icons.Filled.FoodBank),
            Triple(Routes.Recipes, "Recipes", Icons.Filled.MenuBook),
        ).forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = current == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}
