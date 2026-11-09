# FoodPal

An open-source macronutrient and fitness tracker built for **GrapheneOS** (and
any de-Googled Android). No Google proprietary libraries, no analytics, no
cloud sync — every gram of food data stays on-device in a Room database.

Barcode lookups run against a self-hosted [Open Food Facts](https://world.openfoodfacts.org/)
mirror so the app keeps working offline-first without leaking your diet to a
third party. The mirror is part of the sibling `codemill-site` project.

## Screens

- **Sign-up / profile** — body metrics, macro goals, activity level.
- **Dashboard** — daily calories + macros + micronutrients, exercise sheet.
- **Food log** — date carousel, 4 meal cards (breakfast / lunch / dinner / snacks),
  search, barcode scan, copy-from-yesterday.
- **My Foods** — CRUD for custom foods not in Open Food Facts.
- **Recipes** — build a recipe from ingredients, log a portion to a meal.
- **Settings** — editable macro/micronutrient goals, profile.

## Tech stack

- Kotlin + Jetpack Compose (Material3, forced dark theme)
- Room (on-device SQLite) — 7 DAOs, ~30 entities
- OkHttp — only network client, no Play Services
- ZXing (`zxing-android-embedded`) — barcode scanning without Google code scanner
- Repository pattern exposed via a custom `Application` container
- AndroidX Navigation (Compose) for screen routing

## Architecture

```
app/src/main/java/com/foodpal/
├── FoodPalApp.kt              # Application — DI container (repo + api + uid mgr)
├── MainActivity.kt            # Compose host, camera permission bridge
├── data/
│   ├── api/OffApiClient.kt    # Self-hosted OFF mirror client
│   ├── db/                    # Room schema (FoodPalDatabase, Daos, Entities)
│   ├── model/                 # Micros, PieceWeights, Enums
│   └── FoodPalRepository.kt   # single repo over all 7 DAOs
└── ui/
    ├── barcode/BarcodeScanner.kt    # ZXing, no Play Services
    ├── dashboard/                    # Dashboard + Exercise sheet
    ├── food/                         # Food log + meal cards + sheets
    ├── myfood/                       # Custom food CRUD
    ├── recipe/                       # Recipe builder, ingredient picker
    ├── signup/SignUpScreen.kt
    ├── settings/                     # Goals / profile / settings sub-screens
    ├── nav/{FoodPalBottomBar, Routes}.kt
    └── theme/Theme.kt
```

## Build

Open Food Facts API key and base URL are read from `secrets.properties`:

```properties
OFF_API_KEY=your-off-api-key
OFF_BASE_URL=https://your-off-mirror.example.com
```

Then standard Android build:

```
./gradlew assembleDebug      # debug APK
./gradlew installDebug       # install on connected device
```

Requires Android SDK 34+, JDK 17. Minimum runtime: Android 11 (API 30).

## License

MIT — see [LICENSE](LICENSE).
