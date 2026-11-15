package com.foodpal.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodpal.FoodPalApp
import com.foodpal.data.FoodPalRepository
import com.foodpal.data.db.ProfileEntity
import com.foodpal.data.db.SettingsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val goalCalories: Int = 2000,
    val proteinPct: Int = 30,
    val carbsPct: Int = 40,
    val fatPct: Int = 30,
    val saturatedFatG: Double = 32.0,
    val polyunsaturatedFatG: Double = 0.0,
    val monounsaturatedFatG: Double = 0.0,
    val transFatG: Double = 0.0,
    val cholesterolMg: Double = 300.0,
    val sodiumMg: Double = 2300.0,
    val potassiumMg: Double = 3500.0,
    val fiberG: Double = 38.0,
    val sugarG: Double = 108.0,
    val vitaminADvPct: Int = 100,
    val vitaminCDvPct: Int = 100,
    val calciumDvPct: Int = 100,
    val ironDvPct: Int = 100,
    val profile: ProfileState? = null,
)

data class ProfileState(
    val id: Long = 0,
    val nickname: String = "",
    val age: Int = 0,
    val heightCm: Int = 0,
    val weightKg: Double = 0.0,
    val lifestyle: String = "",
    val goal: String = "",
    val goalWeightKg: Double = 0.0,
)

class SettingsViewModel(private val repository: FoodPalRepository) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        repository.observeSettings().map { it?.toState() ?: SettingsState() },
        repository.observeProfile(),
    ) { settings, profile ->
        settings.copy(profile = profile?.toState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun update(transform: (SettingsState) -> SettingsState) {
        viewModelScope.launch {
            val current = state.value
            val next = transform(current)
            repository.saveSettings(next.toEntity())
        }
    }

    fun updateProfile(transform: (ProfileState) -> ProfileState) {
        viewModelScope.launch {
            val current = state.value.profile ?: return@launch
            val next = transform(current)
            repository.updateProfile(next.toEntity())
        }
    }

    private fun SettingsState.toEntity() = SettingsEntity(
        id = 1,
        goalCalories = goalCalories,
        proteinPct = proteinPct,
        carbsPct = carbsPct,
        fatPct = fatPct,
        saturatedFatG = saturatedFatG,
        polyunsaturatedFatG = polyunsaturatedFatG,
        monounsaturatedFatG = monounsaturatedFatG,
        transFatG = transFatG,
        cholesterolMg = cholesterolMg,
        sodiumMg = sodiumMg,
        potassiumMg = potassiumMg,
        fiberG = fiberG,
        sugarG = sugarG,
        vitaminADvPct = vitaminADvPct,
        vitaminCDvPct = vitaminCDvPct,
        calciumDvPct = calciumDvPct,
        ironDvPct = ironDvPct,
    )

    private fun SettingsEntity.toState() = SettingsState(
        goalCalories = goalCalories,
        proteinPct = proteinPct,
        carbsPct = carbsPct,
        fatPct = fatPct,
        saturatedFatG = saturatedFatG,
        polyunsaturatedFatG = polyunsaturatedFatG,
        monounsaturatedFatG = monounsaturatedFatG,
        transFatG = transFatG,
        cholesterolMg = cholesterolMg,
        sodiumMg = sodiumMg,
        potassiumMg = potassiumMg,
        fiberG = fiberG,
        sugarG = sugarG,
        vitaminADvPct = vitaminADvPct,
        vitaminCDvPct = vitaminCDvPct,
        calciumDvPct = calciumDvPct,
        ironDvPct = ironDvPct,
    )

    private fun ProfileEntity.toState() = ProfileState(
        id = id,
        nickname = nickname,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        lifestyle = lifestyle,
        goal = goal,
        goalWeightKg = goalWeightKg,
    )

    private fun ProfileState.toEntity() = ProfileEntity(
        id = id,
        nickname = nickname,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        lifestyle = lifestyle,
        goal = goal,
        goalWeightKg = goalWeightKg,
    )

    companion object {
        fun factory(app: FoodPalApp) = viewModelFactory {
            initializer { SettingsViewModel(app.repository) }
        }
    }
}
