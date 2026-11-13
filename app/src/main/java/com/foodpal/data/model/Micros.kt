package com.foodpal.data.model

import org.json.JSONObject

/**
 * Helpers for working with the `microsJson` column that appears on
 * [com.foodpal.data.db.FoodEntryEntity], [com.foodpal.data.db.CustomFoodEntity]
 * and [com.foodpal.data.db.RecipeIngredientEntity].
 *
 * Keys are the snake_case constants in [MicroNutrientKeys].
 */
object Micros {

    fun parse(json: String): Map<String, Double> {
        if (json.isBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            val out = LinkedHashMap<String, Double>()
            for (key in obj.keys()) {
                val v = obj.optDouble(key, 0.0)
                if (v > 0.0) out[key] = v
            }
            out
        } catch (e: org.json.JSONException) {
            emptyMap()
        }
    }

    /** Scales every nutrient value by `factor` (e.g. 1/2 for half a portion). */
    fun scale(json: String, factor: Double): String {
        val parsed = parse(json)
        if (parsed.isEmpty()) return "{}"
        val obj = JSONObject()
        for ((k, v) in parsed) {
            val scaled = v * factor
            if (scaled > 0.0) obj.put(k, scaled)
        }
        return obj.toString()
    }

    /** Sums two microsJson blobs into one. */
    fun sum(a: String, b: String): String {
        val pa = parse(a).toMutableMap()
        for ((k, v) in parse(b)) {
            pa[k] = (pa[k] ?: 0.0) + v
        }
        val obj = JSONObject()
        for ((k, v) in pa) {
            if (v > 0.0) obj.put(k, v)
        }
        return obj.toString()
    }
}
