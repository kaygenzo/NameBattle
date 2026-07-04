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
    private val KEY_MEANINGS_SEEDED = booleanPreferencesKey("meanings_seeded")

    suspend fun isDbSeeded(): Boolean =
        context.dataStore.data.map { it[KEY_DB_SEEDED] ?: false }.first()

    suspend fun markDbSeeded() {
        context.dataStore.edit { it[KEY_DB_SEEDED] = true }
    }

    suspend fun isMeaningsSeeded(): Boolean =
        context.dataStore.data.map { it[KEY_MEANINGS_SEEDED] ?: false }.first()

    suspend fun markMeaningsSeeded() {
        context.dataStore.edit { it[KEY_MEANINGS_SEEDED] = true }
    }
}
