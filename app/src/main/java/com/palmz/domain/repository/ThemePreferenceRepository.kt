package com.palmz.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Manual light/dark override for the Settings switch. `null` means no override yet — the
 * system setting applies as-is.
 */
interface ThemePreferenceRepository {
    val darkModeOverride: Flow<Boolean?>

    suspend fun setDarkMode(dark: Boolean)
}
