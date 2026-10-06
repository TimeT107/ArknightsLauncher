package com.example.arklauncher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "launcher_settings")

object LauncherPreferences {

    suspend fun saveDockApp(context: Context, slot: String, packageName: String) {
        if (slot.isBlank()) return
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(slot)] = packageName
        }
    }

    fun getDockAppFlow(context: Context, slot: String): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey(slot)]
        }

    // 通用的应用绑定（browser / appstore / calendar / settings / files / gallery 等）
    suspend fun saveBoundApp(context: Context, key: String, packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("bound_$key")] = packageName
        }
    }

    fun getBoundAppFlow(context: Context, key: String): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("bound_$key")]
        }
}