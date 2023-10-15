package com.rib.progressiverecords

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreSettings(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
        val DARK_THEME_KEY = booleanPreferencesKey("isDarkTheme")
        val LOCALE_KEY = stringPreferencesKey("locale")
    }

    val getThemeState: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_THEME_KEY] ?: false
        }

    suspend fun saveThemeState(state: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = state
        }
    }

    val getLocale: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LOCALE_KEY] ?: "en"
        }

    suspend fun saveLocale(locale: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCALE_KEY] = locale
        }
    }
}