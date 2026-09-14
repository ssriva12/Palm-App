package com.palmz.data.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Plain SharedPreferences mirror of the profile's language code (domain.model.Language.code),
 * readable synchronously from Activity.attachBaseContext before DataStore/Hilt injection exists.
 */
object LocaleStore {
    private const val PREFS = "locale_prefs"
    private const val KEY = "language_code"

    fun get(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "en") ?: "en"

    fun set(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, code).apply()
    }
}

/** Wraps [this] so every resource lookup (stringResource, plurals, arrays, ...) resolves against [languageTag]. */
fun Context.withAppLocale(languageTag: String): Context {
    val locale = Locale.forLanguageTag(languageTag)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
