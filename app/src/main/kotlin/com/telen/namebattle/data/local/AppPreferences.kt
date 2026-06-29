package com.telen.namebattle.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "namebattle_prefs")

class AppPreferences(private val context: Context) {

    private val KEY_DB_SEEDED = booleanPreferencesKey("db_seeded")

    suspend fun isDbSeeded(): Boolean =
        context.dataStore.data.map { it[KEY_DB_SEEDED] ?: false }.first()

    suspend fun markDbSeeded() {
        context.dataStore.edit { it[KEY_DB_SEEDED] = true }
    }
}
