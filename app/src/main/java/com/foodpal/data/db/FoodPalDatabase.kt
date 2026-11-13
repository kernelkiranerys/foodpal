package com.foodpal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProfileEntity::class,
        SettingsEntity::class,
        FoodEntryEntity::class,
        CustomFoodEntity::class,
        ExerciseEntryEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class FoodPalDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun settingsDao(): SettingsDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao

    companion object {
        @Volatile private var INSTANCE: FoodPalDatabase? = null

        // v1 -> v2: recipes + recipe_ingredients tables introduced.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recipes` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`portions` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL" +
                        ")",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recipe_ingredients` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`recipeId` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`unit` TEXT NOT NULL, " +
                        "`calories` REAL NOT NULL, " +
                        "`proteinG` REAL NOT NULL, " +
                        "`carbsG` REAL NOT NULL, " +
                        "`fatG` REAL NOT NULL, " +
                        "`microsJson` TEXT NOT NULL, " +
                        "`source` TEXT NOT NULL, " +
                        "`foodId` INTEGER, " +
                        "`barcode` TEXT" +
                        ")",
                )
            }
        }

        // v2 -> v3: add pieceWeightG column to custom_foods for piece-based scaling.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE custom_foods ADD COLUMN pieceWeightG REAL")
            }
        }

        fun get(context: Context): FoodPalDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room
                    .databaseBuilder(context.applicationContext, FoodPalDatabase::class.java, "foodpal.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
