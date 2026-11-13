package com.foodpal.data.api

import android.content.Context
import java.util.UUID

/**
 * Generates and persists a per-install UID (UUID v4).
 *
 * The UID is generated once on first access, stored in SharedPreferences,
 * and registered with the server on app startup. Every subsequent API
 * request includes the UID via the X-Uid header for per-install rate
 * limiting.
 */
class UidManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val uid: String by lazy {
        prefs.getString(KEY_UID, null) ?: run {
            val newUid = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_UID, newUid).apply()
            newUid
        }
    }

    companion object {
        private const val PREFS_NAME = "foodpal_ids"
        private const val KEY_UID = "install_uid"
    }
}
