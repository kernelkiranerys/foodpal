package com.foodpal.data.model

/**
 * Average weight (in grams) of one "piece" of common foods.
 *
 * Used when the user picks "piece" as the unit for an OFF product or
 * custom food. The lookup is by case-insensitive substring match on
 * the food name; longer patterns are checked first to avoid false
 * matches (e.g. "grapefruit" before "grape").
 *
 * Values are USDA reference weights for medium items where applicable.
 */
object PieceWeights {

    // Ordered longest-pattern-first to avoid substring false matches.
    private val weights = listOf(
        // Fruits
        "grapefruit" to 200.0,
        "clementine" to 74.0,
        "mandarin" to 74.0,
        "tangerine" to 88.0,
        "strawberr" to 12.0,
        "blueberr" to 0.5,
        "raspberr" to 1.0,
        "avocado" to 200.0,
        "banana" to 118.0,
        "apple" to 182.0,
        "orange" to 131.0,
        "peach" to 150.0,
        "pear" to 178.0,
        "plum" to 66.0,
        "apricot" to 35.0,
        "mango" to 200.0,
        "kiwi" to 69.0,
        "lemon" to 58.0,
        "lime" to 67.0,
        "fig" to 50.0,
        "date" to 8.0,      // the fruit, not "update" — checked after longer patterns
        "prune" to 9.5,
        // Vegetables
        "potato" to 150.0,
        "tomato" to 123.0,
        "onion" to 150.0,
        "carrot" to 61.0,
        "mushroom" to 18.0,
        "olive" to 3.0,
        "garlic clove" to 3.0,
        "clove of garlic" to 3.0,
        "pickle" to 35.0,
        // Eggs
        "boiled egg" to 50.0,
        "fried egg" to 50.0,
        "poached egg" to 50.0,
        "scrambled egg" to 60.0,
        "omelet" to 100.0,
        "omelette" to 100.0,
        "egg" to 50.0,
        // Bread / baked
        "slice of bread" to 30.0,
        "bread slice" to 30.0,
        "bread" to 30.0,      // assumes per-slice
        "toast" to 30.0,
        "bagel" to 98.0,
        "tortilla" to 49.0,
        "muffin" to 113.0,
        "pancake" to 80.0,
        "cupcake" to 43.0,
        "doughnut" to 47.0,
        "donut" to 47.0,
        "cracker" to 5.0,
        "biscuit" to 16.0,
        "cookie" to 16.0,
        "rice cake" to 9.0,
        // Meat / protein
        "chicken breast" to 174.0,
        "fish fillet" to 142.0,
        "fillet" to 142.0,
        "bacon" to 16.0,      // per strip
        "sausage" to 45.0,
        "meatball" to 30.0,
        "hot dog" to 45.0,
        "hotdog" to 45.0,
        // Dairy
        "cheese slice" to 20.0,
        "slice of cheese" to 20.0,
        "string cheese" to 28.0,
        // Nuts
        "almond" to 1.2,
        "cashew" to 1.5,
        "walnut" to 5.0,
        "peanut" to 0.6,
        "pecan" to 4.0,
        "pistachio" to 1.5,
        "hazelnut" to 4.0,
        "macadamia" to 2.5,
        // Pizza
        "pizza slice" to 107.0,
        "slice of pizza" to 107.0,
    )

    /**
     * Look up the average piece weight for a food by name.
     * Returns null if no match (caller should default to 100g or
     * let the user specify).
     */
    fun lookup(name: String): Double? {
        val lower = name.lowercase()
        for ((pattern, weight) in weights) {
            if (lower.contains(pattern)) return weight
        }
        return null
    }
}
