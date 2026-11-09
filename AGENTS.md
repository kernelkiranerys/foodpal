# FoodPal

Open-source macronutrient & fitness tracker for GrapheneOS. No Google proprietary
libraries; all data stays on device in a Room database.

## Build

The Android SDK is required. Set `ANDROID_HOME` (or open in Android Studio), then:

```
./gradlew assembleDebug
```

Install on a device/emulator:

```
./gradlew installDebug
```

## Architecture

- Kotlin + Jetpack Compose, Material3 (forced dark theme)
- Room for on-device storage (`FoodPalDatabase`)
- Repository pattern (`FoodPalRepository`) exposed via `FoodPalApp`
- Navigation via `androidx.navigation.compose`

## Status

- [x] Project scaffold + Gradle wrapper
- [x] On-device Room schema (profile, settings, food entries, custom foods, exercise, recipes, recipe ingredients)
- [x] Sign-up flow
- [x] Dashboard (Calories / Macros / Micronutrients) wired to live data
- [x] Settings (macro/micronutrient goal editing) — reachable from dashboard "edit" buttons
- [x] Food logging page (date carousel + 4 meal cards + search + custom + copy actions)
- [x] My Foods CRUD (list + add-custom-food form)
- [x] Hosted Open Food Facts mirror + API (https://thecodemill.dev/api/off/search/, /api/off/product/<barcode>/)
- [x] Barcode scanning (ZXing, no Google Play Services) wired into FoodSearchSheet + IngredientPickerSheet
- [x] Recipe builder (list, builder, ingredient picker; per-portion macro/micro totals; add recipe to meal log)
- [x] Exercise calorie logging UI
- [x] Edit/delete for existing day entries (remove only, for now)
