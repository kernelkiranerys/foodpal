package com.foodpal.data.api

import com.foodpal.data.db.FoodEntryEntity
import com.foodpal.data.model.PieceWeights
import org.json.JSONObject

/**
 * A search result or product lookup, in the shape returned by the
 * FoodPal OFF Django service (see site_software/off/views.py).
 *
 * The `micros` keys are snake_case to match the on-device [MicroNutrientKeys]:
 *   saturated_fat, polyunsaturated_fat, monounsaturated_fat, trans_fat,
 *   cholesterol, sodium, potassium, fiber, sugar,
 *   vitamin_a, vitamin_c, calcium, iron
 *
 * `calories` / `proteinG` / `carbsG` / `fatG` are per 100 g/ml (OFF convention).
 * `pieceWeightG` is looked up from [PieceWeights] based on the product name,
 * or null if no match (caller defaults to 100g or lets the user specify).
 */
data class OffFood(
    val type: String,            // "off" | "generic"
    val barcode: String,
    val name: String,
    val brand: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val micros: Map<String, Double>,
    val url: String,
) {
    /** Average grams per piece, looked up from the product name. Null = unknown. */
    val pieceWeightG: Double? by lazy { PieceWeights.lookup(name) }
}

/**
 * Convert an OFF search result to a loggable FoodEntryEntity.
 *
 * OFF products are per 100 g/ml. The user picks an [amount] and [unit];
 * for gram/ml units we scale linearly by amount/100. For "piece" we
 * multiply by [pieceWeightG] (looked up from the name, or 100g fallback).
 */
fun OffFood.toEntry(amount: Double, unit: String): FoodEntryEntity {
    val per100 = when (unit) {
        "g", "ml" -> amount / 100.0
        "kg", "litre" -> amount * 10.0
        "oz" -> amount * 28.3495 / 100.0
        "lbs" -> amount * 453.592 / 100.0
        "floz" -> amount * 29.5735 / 100.0
        "piece" -> (amount * (pieceWeightG ?: 100.0)) / 100.0
        else -> amount / 100.0
    }
    val source = if (type == "off") "off" else "generic"
    return FoodEntryEntity(
        date = "",
        meal = "",
        name = name,
        amount = amount,
        unit = unit,
        calories = calories * per100,
        proteinG = proteinG * per100,
        carbsG = carbsG * per100,
        fatG = fatG * per100,
        microsJson = if (micros.isEmpty()) "{}" else {
            val obj = JSONObject()
            for ((k, v) in micros) obj.put(k, v)
            obj.toString()
        },
        source = source,
        foodId = null,
    )
}
