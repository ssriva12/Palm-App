package com.palmz.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.palmz.domain.model.Gender
import com.palmz.domain.model.UserProfile
import com.palmz.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.profileStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

private object Keys {
    val NAME = stringPreferencesKey("name")
    val GENDER = stringPreferencesKey("gender")
    val DOB_EPOCH_DAY = longPreferencesKey("dob_epoch_day")
    val BIRTH_TIME_SECONDS = intPreferencesKey("birth_time_seconds")
    val BIRTH_TIME_KNOWN = booleanPreferencesKey("birth_time_known")
    val BIRTH_PLACE = stringPreferencesKey("birth_place")
    val LOCALE = stringPreferencesKey("locale")
}

@Singleton
class DataStoreProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ProfileRepository {

    override val profile: Flow<UserProfile> = context.profileStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toProfile() }

    override suspend fun update(transform: (UserProfile) -> UserProfile) {
        context.profileStore.edit { prefs ->
            prefs.write(transform(prefs.toProfile()))
        }
    }

    override suspend fun clear() {
        context.profileStore.edit { it.clear() }
    }
}

private fun Preferences.toProfile(): UserProfile = UserProfile(
    name = this[Keys.NAME].orEmpty(),
    gender = this[Keys.GENDER]?.let { runCatching { Gender.valueOf(it) }.getOrNull() },
    dob = this[Keys.DOB_EPOCH_DAY]?.let(LocalDate::ofEpochDay),
    birthTime = this[Keys.BIRTH_TIME_SECONDS]?.let { LocalTime.ofSecondOfDay(it.toLong()) },
    birthTimeKnown = this[Keys.BIRTH_TIME_KNOWN] ?: false,
    birthPlace = this[Keys.BIRTH_PLACE].orEmpty(),
    languageCode = this[Keys.LOCALE] ?: "en",
)

private fun androidx.datastore.preferences.core.MutablePreferences.write(p: UserProfile) {
    this[Keys.NAME] = p.name
    if (p.gender != null) this[Keys.GENDER] = p.gender.name else remove(Keys.GENDER)
    if (p.dob != null) this[Keys.DOB_EPOCH_DAY] = p.dob.toEpochDay() else remove(Keys.DOB_EPOCH_DAY)
    if (p.birthTime != null) this[Keys.BIRTH_TIME_SECONDS] = p.birthTime.toSecondOfDay() else remove(Keys.BIRTH_TIME_SECONDS)
    this[Keys.BIRTH_TIME_KNOWN] = p.birthTimeKnown
    this[Keys.BIRTH_PLACE] = p.birthPlace
    this[Keys.LOCALE] = p.languageCode
}
