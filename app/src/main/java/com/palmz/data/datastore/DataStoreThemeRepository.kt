package com.palmz.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.palmz.domain.repository.ThemePreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeStore: DataStore<Preferences> by preferencesDataStore(name = "theme")
private val DARK_MODE_OVERRIDE = booleanPreferencesKey("dark_mode_override")

@Singleton
class DataStoreThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ThemePreferenceRepository {

    override val darkModeOverride: Flow<Boolean?> = context.themeStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[DARK_MODE_OVERRIDE] }

    override suspend fun setDarkMode(dark: Boolean) {
        context.themeStore.edit { it[DARK_MODE_OVERRIDE] = dark }
    }
}
