package com.foodpal.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodpal.FoodPalApp
import com.foodpal.data.FoodPalRepository
import com.foodpal.data.db.ExerciseEntryEntity
import com.foodpal.data.db.FoodEntryEntity
import com.foodpal.data.db.ProfileEntity
import com.foodpal.data.db.SettingsEntity
import com.foodpal.data.model.Micros
import com.foodpal.data.model.MicroNutrientKeys
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MicroStat(
    val key: String,
    val name: String,
    val consumed: Double,
    val goal: Double,
    val unit: String,
)

data class DashboardState(
    val date: LocalDate = LocalDate.now(),
    val caloriesConsumed: Int = 0,
    val exerciseCalories: Int = 0,
    val exerciseEntries: List<ExerciseEntryEntity> = emptyList(),
    val goalCalories: Int = 2000,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val proteinGoalG: Double = 150.0,
    val carbsGoalG: Double = 200.0,
    val fatGoalG: Double = 67.0,
    val micros: List<MicroStat> = emptyList(),
)

class DashboardViewModel(
    private val repository: FoodPalRepository,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        repository.observeEntries(today.toString()),
        repository.observeSettings(),
        repository.observeExercise(today.toString()),
    ) { entries, settings, exercise ->
        compute(entries, settings, exercise)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun addExercise(name: String, caloriesBurned: Int) {
        if (name.isBlank() || caloriesBurned <= 0) return
        viewModelScope.launch {
            repository.addExercise(
                ExerciseEntryEntity(
                    date = today.toString(),
                    name = name.trim(),
                    caloriesBurned = caloriesBurned,
                ),
            )
        }
    }

    fun deleteExercise(entry: ExerciseEntryEntity) {
        viewModelScope.launch { repository.deleteExercise(entry) }
    }

    private fun compute(
        entries: List<FoodEntryEntity>,
        settings: SettingsEntity?,
        exercise: List<ExerciseEntryEntity>,
    ): DashboardState {
        val goalCal = settings?.goalCalories ?: 2000
        val proteinPct = settings?.proteinPct ?: 30
        val carbsPct = settings?.carbsPct ?: 40
        val fatPct = settings?.fatPct ?: 30
        val proteinGoal = goalCal * proteinPct / 100.0 / 4.0
        val carbsGoal = goalCal * carbsPct / 100.0 / 4.0
        val fatGoal = goalCal * fatPct / 100.0 / 9.0

        val consumed = entries.sumOf { it.calories }.toInt()
        val exerciseCal = exercise.sumOf { it.caloriesBurned }

        // Aggregate per-nutrient totals across the day's food entries.
        val aggregatedMicros = entries.fold("{}") { acc, e ->
            Micros.sum(acc, e.microsJson)
        }
        fun consumed(key: String): Double =
            Micros.parse(aggregatedMicros)[key]?.let { it } ?: 0.0

        val micros = listOf(
            MicroStat(MicroNutrientKeys.SATURATED_FAT, "Saturated Fat", consumed(MicroNutrientKeys.SATURATED_FAT), settings?.saturatedFatG ?: 32.0, "g"),
            MicroStat(MicroNutrientKeys.POLYUNSATURATED_FAT, "Polyunsaturated Fat", consumed(MicroNutrientKeys.POLYUNSATURATED_FAT), settings?.polyunsaturatedFatG ?: 0.0, "g"),
            MicroStat(MicroNutrientKeys.MONOUNSATURATED_FAT, "Monounsaturated Fat", consumed(MicroNutrientKeys.MONOUNSATURATED_FAT), settings?.monounsaturatedFatG ?: 0.0, "g"),
            MicroStat(MicroNutrientKeys.TRANS_FAT, "Trans Fat", consumed(MicroNutrientKeys.TRANS_FAT), settings?.transFatG ?: 0.0, "g"),
            MicroStat(MicroNutrientKeys.CHOLESTEROL, "Cholesterol", consumed(MicroNutrientKeys.CHOLESTEROL), settings?.cholesterolMg ?: 300.0, "mg"),
            MicroStat(MicroNutrientKeys.SODIUM, "Sodium", consumed(MicroNutrientKeys.SODIUM), settings?.sodiumMg ?: 2300.0, "mg"),
            MicroStat(MicroNutrientKeys.POTASSIUM, "Potassium", consumed(MicroNutrientKeys.POTASSIUM), settings?.potassiumMg ?: 3500.0, "mg"),
            MicroStat(MicroNutrientKeys.FIBER, "Fiber", consumed(MicroNutrientKeys.FIBER), settings?.fiberG ?: 38.0, "g"),
            MicroStat(MicroNutrientKeys.SUGAR, "Sugar", consumed(MicroNutrientKeys.SUGAR), settings?.sugarG ?: 108.0, "g"),
            MicroStat(MicroNutrientKeys.VITAMIN_A, "Vitamin A", consumed(MicroNutrientKeys.VITAMIN_A), (settings?.vitaminADvPct ?: 100).toDouble(), "%DV"),
            MicroStat(MicroNutrientKeys.VITAMIN_C, "Vitamin C", consumed(MicroNutrientKeys.VITAMIN_C), (settings?.vitaminCDvPct ?: 100).toDouble(), "%DV"),
            MicroStat(MicroNutrientKeys.CALCIUM, "Calcium", consumed(MicroNutrientKeys.CALCIUM), (settings?.calciumDvPct ?: 100).toDouble(), "%DV"),
            MicroStat(MicroNutrientKeys.IRON, "Iron", consumed(MicroNutrientKeys.IRON), (settings?.ironDvPct ?: 100).toDouble(), "%DV"),
        )

        return DashboardState(
            date = today,
            caloriesConsumed = consumed,
            exerciseCalories = exerciseCal,
            exerciseEntries = exercise,
            goalCalories = goalCal,
            proteinG = entries.sumOf { it.proteinG },
            carbsG = entries.sumOf { it.carbsG },
            fatG = entries.sumOf { it.fatG },
            proteinGoalG = proteinGoal,
            carbsGoalG = carbsGoal,
            fatGoalG = fatGoal,
            micros = micros,
        )
    }

    companion object {
        fun factory(app: FoodPalApp) = viewModelFactory {
            initializer { DashboardViewModel(app.repository) }
        }
    }
}
