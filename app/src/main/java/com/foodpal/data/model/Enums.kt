package com.foodpal.data.model

enum class Lifestyle(val label: String) {
    SEDENTARY("Sedentary"),
    MODERATE("Moderate"),
    ACTIVE("Active"),
    VERY_ACTIVE("Very active"),
}

enum class Goal(val label: String) {
    GAIN_MUSCLE("Gain muscle"),
    LOSE_BODY_FAT("Lose body fat"),
    IMPROVE_CARDIO("Improve cardio health"),
}

enum class Meal(val key: String, val label: String) {
    BREAKFAST("breakfast", "Breakfast"),
    LUNCH("lunch", "Lunch"),
    DINNER("dinner", "Dinner"),
    SNACKS("snacks", "Snacks"),
}

enum class FoodUnit(val symbol: String) {
    GRAM("g"),
    KILOGRAM("kg"),
    OUNCE("oz"),
    POUND("lbs"),
    MILLILITRE("ml"),
    LITRE("litre"),
    FLOUNCE("floz"),
    PIECE("piece"),
}

object MicroNutrientKeys {
    const val SATURATED_FAT = "saturated_fat"
    const val POLYUNSATURATED_FAT = "polyunsaturated_fat"
    const val MONOUNSATURATED_FAT = "monounsaturated_fat"
    const val TRANS_FAT = "trans_fat"
    const val CHOLESTEROL = "cholesterol"
    const val SODIUM = "sodium"
    const val POTASSIUM = "potassium"
    const val FIBER = "fiber"
    const val SUGAR = "sugar"
    const val VITAMIN_A = "vitamin_a"
    const val VITAMIN_C = "vitamin_c"
    const val CALCIUM = "calcium"
    const val IRON = "iron"
}
