package com.telen.namebattle.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "namebattle_prefs")

class AppPreferences(private val context: Context) {

    private val KEY_DB_SEEDED = booleanPreferencesKey("db_seeded")
    private val KEY_MEANINGS_VERSION = intPreferencesKey("meanings_version")
    private val KEY_MEANINGS_CHECKSUM = stringPreferencesKey("meanings_checksum")

    suspend fun isDbSeeded(): Boolean =
        context.dataStore.data.map { it[KEY_DB_SEEDED] ?: false }.first()

    suspend fun markDbSeeded() {
        context.dataStore.edit { it[KEY_DB_SEEDED] = true }
    }

    suspend fun getMeaningsVersion(): Int =
        context.dataStore.data.map { it[KEY_MEANINGS_VERSION] ?: 0 }.first()

    suspend fun getMeaningsChecksum(): String =
        context.dataStore.data.map { it[KEY_MEANINGS_CHECKSUM] ?: "" }.first()

    suspend fun saveMeaningsMeta(version: Int, checksum: String) {
        context.dataStore.edit {
            it[KEY_MEANINGS_VERSION] = version
            it[KEY_MEANINGS_CHECKSUM] = checksum
        }
    }
}
