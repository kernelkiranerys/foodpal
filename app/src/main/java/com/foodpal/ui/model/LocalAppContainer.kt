package com.foodpal.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.foodpal.FoodPalApp
import com.foodpal.data.FoodPalRepository

val LocalAppContainer = compositionLocalOf<FoodPalRepository> {
    error("FoodPalRepository not provided")
}

@Composable
fun rememberRepository(): FoodPalRepository {
    val context = LocalContext.current
    val app = context.applicationContext as FoodPalApp
    return app.repository
}
