package com.foodpal.ui.myfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodpal.FoodPalApp
import com.foodpal.data.FoodPalRepository
import com.foodpal.data.db.CustomFoodEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyFoodViewModel(private val repository: FoodPalRepository) : ViewModel() {

    val foods: StateFlow<List<CustomFoodEntity>> = repository.observeCustomFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(food: CustomFoodEntity) {
        viewModelScope.launch { repository.addCustomFood(food) }
    }

    fun delete(food: CustomFoodEntity) {
        viewModelScope.launch { repository.deleteCustomFood(food) }
    }

    companion object {
        fun factory(app: FoodPalApp) = viewModelFactory {
            initializer { MyFoodViewModel(app.repository) }
        }
    }
}
